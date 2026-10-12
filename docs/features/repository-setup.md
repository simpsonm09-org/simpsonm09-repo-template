# Repository setup

Use the standard's [adoption guide](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/adoption.md)
when starting a repository from this template. Keep shared workflow references
pinned to one standard commit. Copy shared scripts from that commit and check
their local changes before updating them.

## Decide on day one

Decide these before the first pull request. Each item links to the standard for the rule.

- **Coverage.** Keep the `coverage` job in `.github/workflows/ci.yml` when the repository has code the shared coverage workflow can measure, as this template does. Otherwise remove the job and add an entry on `ci/coverage` to the standard's `docs/exceptions.json`, with the reason. See [governance](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/governance.md#required-checks).
- **CodeQL.** Add `.github/workflows/codeql.yml` from the standard's `templates/workflows/codeql.yml` when the repository has executable source in a language CodeQL supports. Replace the `LANGUAGES` placeholder. This template carries none. See [automation](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/automation.md#codeql).
- **Pin drift.** Keep `.github/workflows/pin-update.yml`. It opens a tracking issue when the standard pin in `ci.yml` falls behind the standard's `main`. See [automation](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/automation.md#dependency-and-action-updates).
- **Secrets.** A human reaches a secret only through dev-setup's `with-secrets`, and an agent through `with-vault --role agent`. Never load secrets through `.envrc`. The `.env.example` file names the local values and loads nothing.

## Local settings

The optional local file is `.env`. Its committed sibling is `.env.example`, both at
the repository root. Keep `.worktreeinclude` at the root and list `.env` there. The
`.gitignore` rule for `.env` ignores the local file, and `!.env.example` keeps the
sibling tracked. Both the local file and any retired `.envrc` stay ignored. Never
commit or delete a real local file during adoption.

The template does not load secrets into a shell on directory entry. For a command
that needs a secret, use dev-setup's `with-secrets [--offline] [--require NAMES] <tool>` human wrapper. Use
`with-vault --role agent <tool>` for the agent broker path. The template's
application reads no secret, and its settings live in
`src/main/resources/application.yaml`. An example file alone loads nothing. A
service that needs a secret reads it from the environment, and a wrapper sets it
for one command.

## Validation

Run `just lint`, `just complexity`, `just test`, and `just coverage`. `just verify`
runs lint, complexity, and tests. The recipes run the pinned tools through
`mise exec --`, so `just test` needs no extra prefix once `mise install` has run.
`coverage` also runs `node`, which `mise.toml` does not pin. It uses the Node on PATH.

Coverage writes `coverage/lcov.info`. The shared CI workflow runs the pinned
standard's patch gate at 80 percent of changed lines and 70 percent of changed
branches. The patch script belongs to the standard checkout, not this template's
scripts.

From a checkout of the pinned standard, run `node scripts/check-standard.mjs <repository-path>`. To also apply the exception file, run `node scripts/check-repo.mjs <repository-path> --repo <owner>/<repo>`.
The [validation guide](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/validation.md)
lists all groups, including `env` and `subprocess`. Every Node child-process call
must pass a literal `windowsHide: true` in its options, including calls in copied
shared scripts. The template's prune script follows this rule.
