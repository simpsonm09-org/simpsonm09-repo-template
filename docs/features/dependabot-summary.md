# Dependabot summary

`dependabot-summary.yml` writes an optional, read-only reviewer summary on Dependabot pull
requests in this repository. It is a one-repository pilot: it is not part of `templates/` and
is not propagated to the fleet.

## What it does

The workflow runs on `pull_request` and writes three values to the job summary: dependency
names, dependency type (`direct:production`, `direct:development`, or `indirect`), and update
type (for example `version-update:semver-minor`). The values come from
`dependabot/fetch-metadata`, pinned to v3.1.0 at commit
`25dd0e34f4fe68f24cc83900b1fe3fe149efef98`. Dependabot's native labels already identify the
ecosystem; the summary adds the dependency and update types in the run view, which the labels
do not show.

The job summarizes only when both conditions hold:

- `github.repository` is exactly `simpsonm09-org/simpsonm09-repo-template`.
- The pull request author is `dependabot[bot]`.

The event sender is not checked. A maintainer who reopens or edits a bot pull request creates a
new event whose sender is that maintainer, and the pull request is still a Dependabot pull
request, so it still gets a summary. A workflow re-run of the original event keeps the original
sender, so it is unaffected either way.

Every other pull request runs a neutral step and reports success. The job is never skipped, so
it cannot leave a skipped status on a head commit that otherwise has every check green.

## Data handling

All metadata is treated as untrusted input. The renderer runs from the workflow file itself,
so the job needs no checkout. It removes control characters and bidirectional overrides,
collapses line breaks, truncates long values, and escapes HTML and Markdown syntax before
writing the summary. A dependency name cannot start a heading, open a tag, create a link, or
break out of the bullet.

The summary is advisory. Its fixed text says that the job cannot approve, label, merge, or
change the merge gate, and that missing values are not evidence of safety. The action's
commit verification is not a trust decision, and no value from it is presented as one.

## Boundaries

- The trigger is `pull_request` only. The workflow does not use `pull_request_target` or
  `workflow_run`.
- `permissions: {}` at the workflow and `pull-requests: read` at the job are the whole token
  surface. `contents: read` is not requested; the action documents `pull-requests: read` for
  this metadata.
- There is no checkout, no pull request code, no dependency install, no persistent cache, and
  no secret.
- The action is the only third-party code, and it is pinned to a reviewed commit.
- `alert-lookup` and `compat-lookup` are not set. The action treats any non-empty value,
  including `false`, as enabled; both options need a broader token or extra network egress.
- Human review, required checks, and all native Dependabot configuration are unchanged.

## Failure modes

- **Human or non-target pull request.** The neutral step runs, no summary is written, and the
  job succeeds.
- **Dependabot pull request the action cannot describe.** The fetch step fails and the job
  fails. The job is not a required check, so the merge gate does not change.
- **Missing step summary path.** The renderer exits non-zero instead of discarding output.
- **Missing or whitespace-only metadata.** The matching line reads `not reported`; the
  renderer does not invent a value.

## Validate

`just test-workflow` runs the offline tests. They extract the renderer from the workflow and
execute that exact source against normal, hostile, oversized, and missing metadata. They use
no network, no GitHub token, and no write.

`just lint` runs actionlint and zizmor over the workflow with the other linters. `just verify`
runs `test-workflow` after the application tests, and the CI `test` job calls
`just test-workflow` explicitly, so the tests gate the pull request either way.

Hosted behavior is not verified until a GitHub-hosted run happens. The workflow is read from
the pull request's definition, and Dependabot's `pull_request` runs receive a read-only token
by default. Official guidance is inconsistent about widening that token; this workflow asks
for read scopes only, so the default is sufficient.

## Decision gate and rollback

Observe two real Dependabot cycles. Keep the pilot only if it ran on every bot pull request,
reported the types correctly, and reviewers did not call it noise. Do not copy it to
`templates/` or another repository before that gate.

Rollback: delete `.github/workflows/dependabot-summary.yml`, this page and its index and
manifest entries, the `test-workflow` recipe, the CI step that calls it, and
`scripts/dependabot-summary-workflow.mjs` with `scripts/dependabot-summary.test.mjs`. Nothing
else changes.
