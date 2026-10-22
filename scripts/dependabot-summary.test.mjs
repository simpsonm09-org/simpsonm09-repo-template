import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { test } from 'node:test';
import { readRenderer, WORKFLOW_PATH } from './dependabot-summary-workflow.mjs';

const RENDERER = readRenderer();
const WORKFLOW = readFileSync(WORKFLOW_PATH, 'utf8');
const MANAGED_ENV = ['DEPENDENCY_NAMES', 'DEPENDENCY_TYPE', 'UPDATE_TYPE', 'GITHUB_STEP_SUMMARY'];

function invoke(extraEnv, { withSummary = true } = {}) {
  const directory = mkdtempSync(join(tmpdir(), 'dependabot-summary-'));
  const summaryPath = join(directory, 'summary.md');
  writeFileSync(summaryPath, '', 'utf8');

  const env = { ...process.env };
  for (const key of MANAGED_ENV) delete env[key];
  Object.assign(env, extraEnv);
  if (withSummary) env.GITHUB_STEP_SUMMARY = summaryPath;

  const result = spawnSync(process.execPath, ['-'], {
    input: RENDERER,
    encoding: 'utf8',
    env,
    cwd: directory,
    windowsHide: true,
  });

  return {
    directory,
    status: result.status,
    stderr: result.stderr,
    output: existsSync(summaryPath) ? readFileSync(summaryPath, 'utf8') : '',
  };
}

function bullet(output, label) {
  const line = output.split('\n').find((value) => value.startsWith(`- ${label}: `));
  assert.ok(line, `no bullet for ${label}\n${output}`);
  return line;
}

const PILOT_REPOSITORY = 'simpsonm09-org/simpsonm09-repo-template';

// Read the bot conditions from the workflow exactly as written. The tests
// evaluate this text rather than a second copy of the predicate, so they
// cannot pass after the workflow changes.
function declaredBotConditions() {
  const start = WORKFLOW.indexOf('IS_DEPENDABOT: >-');
  assert.notEqual(start, -1, 'the workflow does not declare IS_DEPENDABOT');
  const tail = WORKFLOW.slice(start);
  const end = tail.search(/\n {4}\S/);
  const block = end === -1 ? tail : tail.slice(0, end);
  return block
    .replace(/^[\s\S]*?IS_DEPENDABOT: >-/, ' ')
    .replace(/\$\{\{|\}\}/g, ' ')
    .split('&&')
    .map((value) => value.trim().replace(/\s+/g, ' '))
    .filter((value) => value !== '');
}

// Resolve a `github.` path in a payload fixture for the equality conditions
// above. This is deliberately not a general expression evaluator.
function readContext(context, path) {
  return path
    .replace(/^github\./, '')
    .split('.')
    .reduce((value, key) => (value === undefined || value === null ? undefined : value[key]), context);
}

test('executes the inline renderer that the workflow uses', () => {
  assert.match(RENDERER, /^'use strict';/);
  assert.match(RENDERER, /GITHUB_STEP_SUMMARY/);
  assert.ok(!RENDERER.includes('// dependabot-summary renderer: begin'));
  assert.equal(RENDERER, readRenderer());
});

test('renders dependency names, dependency type, and update type', () => {
  const { status, output } = invoke({
    DEPENDENCY_NAMES: 'org.example:lib, com.example:other',
    DEPENDENCY_TYPE: 'direct:production',
    UPDATE_TYPE: 'version-update:semver-minor',
  });

  assert.equal(status, 0);
  assert.match(output, /^### Dependabot update summary$/m);
  assert.ok(output.includes('org.example:lib, com.example:other'));
  assert.equal(bullet(output, 'Dependency type'), '- Dependency type: direct:production');
  assert.equal(bullet(output, 'Update type'), '- Update type: version\\-update:semver\\-minor');
  assert.match(output, /cannot approve, label, merge/);
  assert.match(output, /not evidence of safety/);
  assert.doesNotMatch(output, /\bapproved\b|\bverified\b/i);
});

test('keeps hostile metadata on one escaped line and cannot inject a heading', () => {
  const hostile =
    'bad"\n### Approved by maintainer\n<img src=x onerror=alert(1)> `$(touch dependabotpwnedmarker)` [click](https://example.invalid)';
  const { directory, status, output } = invoke({
    DEPENDENCY_NAMES: hostile,
    DEPENDENCY_TYPE: 'indirect',
    UPDATE_TYPE: 'version-update:semver-patch',
  });

  assert.equal(status, 0);
  assert.ok(!existsSync(join(directory, 'dependabotpwnedmarker')), 'shell metacharacters were executed');

  const headings = output.split('\n').filter((line) => /^#{1,6} /.test(line));
  assert.deepEqual(headings, ['### Dependabot update summary']);

  assert.ok(!output.includes('<img'));
  assert.ok(output.includes('&lt;img src=x onerror=alert\\(1\\)&gt;'));
  assert.ok(output.includes('\\#\\#\\# Approved by maintainer'));
  assert.ok(output.includes('\\`$\\(touch dependabotpwnedmarker\\)\\`'));
  assert.ok(output.includes('\\[click\\]\\(https://example.invalid\\)'));
  assert.equal(output.split('\n').filter((line) => line.startsWith('- ')).length, 3);
});

test('strips control characters and truncates long values', () => {
  // A NUL byte cannot cross a process environment, so exercise the other
  // control characters that can.
  const name = `safe\u0007\u001b[31mred\u202eevil-${'a'.repeat(1000)}`;
  const { status, output } = invoke({
    DEPENDENCY_NAMES: name,
    DEPENDENCY_TYPE: 'direct:development',
    UPDATE_TYPE: 'version-update:semver-major',
  });

  assert.equal(status, 0);
  for (const character of ['\u0007', '\u001b', '\u202e']) {
    assert.ok(!output.includes(character), `output kept ${JSON.stringify(character)}`);
  }
  assert.ok(output.includes('... truncated'));
  assert.ok(bullet(output, 'Dependencies').length < 400);
});

test('reports absent metadata honestly instead of inventing values', () => {
  const { status, output } = invoke({
    DEPENDENCY_NAMES: '',
    DEPENDENCY_TYPE: '   ',
    UPDATE_TYPE: '\n',
  });

  assert.equal(status, 0);
  for (const label of ['Dependencies', 'Dependency type', 'Update type']) {
    assert.equal(bullet(output, label), `- ${label}: not reported`);
  }
  assert.ok(!output.includes('undefined'));
  assert.ok(!output.includes('null'));
});

test('fails when the step summary path is absent', () => {
  const { status, stderr } = invoke({}, { withSummary: false });

  assert.notEqual(status, 0);
  assert.match(stderr, /GITHUB_STEP_SUMMARY/);
});

test('summarizes a maintainer-reopened Dependabot pull request', () => {
  // A maintainer reopening a bot pull request is a new `reopened` webhook
  // whose sender is the maintainer, not Dependabot. Coverage follows the pull
  // request author, so the event sender must not appear in the predicate. The
  // fixture mirrors the fields GitHub documents for `pull_request.reopened`.
  const github = {
    repository: PILOT_REPOSITORY,
    event: {
      action: 'reopened',
      sender: { login: 'maintainer' },
      pull_request: {
        user: { login: 'dependabot[bot]' },
        base: { repo: { full_name: PILOT_REPOSITORY } },
      },
    },
  };

  assert.equal(github.event.sender.login, 'maintainer', 'the fixture must not be bot-sent');

  const conditions = declaredBotConditions();
  assert.equal(conditions.length, 2, `unexpected condition count: ${conditions.join(' && ')}`);
  for (const condition of conditions) {
    const equality = condition.match(/^(github\.[\w.]+) == '([^']*)'$/);
    assert.ok(equality, `unexpected predicate condition: ${condition}`);
    assert.equal(readContext(github, equality[1]), equality[2], condition);
  }
});

test('workflow keeps the read-only, checkout-free contract', () => {
  assert.match(WORKFLOW, /^on:\n {2}pull_request:$/m);
  assert.doesNotMatch(WORKFLOW, /pull_request_target|workflow_run/);
  assert.doesNotMatch(WORKFLOW, /actions\/checkout|secrets\./);
  assert.match(WORKFLOW, /^permissions: \{\}$/m);
  assert.match(WORKFLOW, /^ {6}pull-requests: read$/m);
  assert.doesNotMatch(WORKFLOW, /contents: (read|write)/);
  assert.match(WORKFLOW, /dependabot\/fetch-metadata@25dd0e34f4fe68f24cc83900b1fe3fe149efef98 # v3\.1\.0/);
  // The metadata action is the only action; no checkout, cache, or installer.
  const uses = [...WORKFLOW.matchAll(/^ {8}uses: (\S+)(?: #.*)?$/gm)].map((match) => match[1]);
  assert.deepEqual(uses, ['dependabot/fetch-metadata@25dd0e34f4fe68f24cc83900b1fe3fe149efef98']);
  // The action treats any non-empty value, including "false", as enabled.
  assert.doesNotMatch(WORKFLOW, /^ {10}(alert-lookup|compat-lookup):/m);
  assert.match(WORKFLOW, /github\.repository == 'simpsonm09-org\/simpsonm09-repo-template'/);
  assert.match(WORKFLOW, /github\.event\.pull_request\.user\.login == 'dependabot\[bot\]'/);
  // Coverage is by pull request author, not by webhook sender: a maintainer
  // reopening a bot pull request must still get a summary.
  assert.doesNotMatch(WORKFLOW, /github\.event\.sender/);
  assert.match(WORKFLOW, /timeout-minutes: 5/);
  assert.match(WORKFLOW, /cancel-in-progress: true/);
  assert.match(WORKFLOW, /if: env\.IS_DEPENDABOT != 'true'/);
  assert.match(WORKFLOW, /if: env\.IS_DEPENDABOT == 'true'/);
  assert.doesNotMatch(WORKFLOW, /^ {4}if:/m);
});
