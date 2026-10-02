import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { test } from 'node:test';
import { fileURLToPath } from 'node:url';
import { jacocoToLcov } from './jacoco-to-lcov.mjs';

const HERE = dirname(fileURLToPath(import.meta.url));
const FIXTURE = readFileSync(resolve(HERE, 'fixtures/jacoco.xml'), 'utf8');

function recordFor(lcov, sourcePath) {
  const found = lcov
    .split('end_of_record')
    .map((record) => record.trim())
    .find((record) => record.includes(`SF:${sourcePath}`));
  assert.ok(found, `no record for ${sourcePath}\n${lcov}`);
  return found;
}

test('emits one SF record per source file with a repo-relative path', () => {
  const lcov = jacocoToLcov(FIXTURE);

  assert.match(lcov, /SF:src\/main\/kotlin\/com\/simpsonm09\/template\/service\/ItemService\.kt/);
  assert.match(lcov, /SF:src\/main\/kotlin\/com\/simpsonm09\/template\/api\/ItemController\.kt/);
  assert.equal(lcov.split('end_of_record').length - 1, 2);
});

test('marks a line covered only when its covered-instruction count is above zero', () => {
  const record = recordFor(jacocoToLcov(FIXTURE), 'src/main/kotlin/com/simpsonm09/template/service/ItemService.kt');

  assert.match(record, /^DA:12,1$/m);
  assert.match(record, /^DA:13,1$/m);
  assert.match(record, /^DA:14,0$/m);
});

test('counts LF and LH per record', () => {
  const lcov = jacocoToLcov(FIXTURE);
  const service = recordFor(lcov, 'src/main/kotlin/com/simpsonm09/template/service/ItemService.kt');
  const controller = recordFor(lcov, 'src/main/kotlin/com/simpsonm09/template/api/ItemController.kt');

  assert.match(service, /^LF:3$/m);
  assert.match(service, /^LH:2$/m);
  assert.match(controller, /^LF:2$/m);
  assert.match(controller, /^LH:1$/m);
});

test('honours a custom source root', () => {
  const lcov = jacocoToLcov(FIXTURE, { sourceRoot: 'src/main/java' });

  assert.match(lcov, /SF:src\/main\/java\/com\/simpsonm09\/template\/service\/ItemService\.kt/);
});

test('returns an empty string when the report has no source lines', () => {
  assert.equal(jacocoToLcov('<report name="empty"></report>'), '');
});
