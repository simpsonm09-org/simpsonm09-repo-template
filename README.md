# repo-template

A generated-repo starting point. It is a working Python project with a placeholder package, wired to the shared standard in [`repo-standard`](https://github.com/simpsonm09-org/simpsonm09-repo-standard).

Use it as a GitHub template repository.

```bash
gh repo create simpsonm09-org/<new-repo> --template simpsonm09-org/simpsonm09-repo-template --private
```

## What is included

- A small placeholder package under `src/app` with tests under `tests`.
- A CI caller that runs lint, aislop, and security from `repo-standard`, plus the tests.
- A `justfile` task runner, with `just` pinned in `mise.toml`.
- Flint with ruff, actionlint, and zizmor pinned in `mise.toml`.
- aislop, Trivy, and editor and git conventions.
- Agent and skill templates under `templates/` for project-local OpenCode setup.
- Governance and community files. `CONTRIBUTING.md` carries the branch, fork, and CI rules; `SECURITY.md` and `CODEOWNERS` are the shared community files.

## Layout

- `justfile` is the one local entry point for a human or an agent. Run `just --list` to see the recipes. Keep each recipe a thin call to a portable tool or a script under `scripts/`.
- `mise.toml` pins the tools, points mise at the project `.venv`, and holds the `mise` tasks that the justfile calls.
- `scripts/` holds operational scripts when the repository needs them. Ship a bash and a PowerShell version with the same behavior and exit codes. See the standard's [`docs/scripting.md`](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/scripting.md).
- `templates/agents/` and `templates/skills/` hold OpenCode profiles to copy into `.opencode/` when the repository ships project-local setup. See the standard's [`docs/agents-and-skills.md`](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/agents-and-skills.md).
- `.github/workflows/ci.yml` is the caller for the shared lint, aislop, and security workflows, plus this repository's test job.
- `.github/config/` holds the Flint configuration.

## Conventions

- Task runner. `just` is the single local entry point. `just install` pins the tools and installs the package with its test dependencies, `just lint` and `just aislop` run the shared gates, `just test` runs the suite, and `just verify` runs lint and test. See the standard's [`docs/task-runner.md`](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/task-runner.md).
- Scripts. Operational scripts live in `scripts/` and ship in both bash and PowerShell with the same behavior. Read paths from the script location, print a dry run by default, and exit non-zero on a real failure.
- Testing. The shared jobs do not run tests. Add the repository's own `test` job to the caller workflow next to them, and keep the same command in the `mise` `test` task. See the standard's [`docs/testing.md`](https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/testing.md).
- Agents and skills. Copy a profile from `templates/` into `.opencode/agents/<id>.md` or `.opencode/skills/<id>/SKILL.md`. Keep secrets and machine-specific paths out of a committed profile.

## After creating a repository

1. Rename the `app` package and update `pyproject.toml`.
2. Run `flint init` to reconcile the tool pins.
3. Replace the `test` task in `mise.toml` with the repository test command.
4. Run the ruleset apply script from `repo-standard` once the repository is public.
5. Replace this README.

## Local commands

```bash
just install
just --list
just lint
just test
just verify
```

## License

MIT. See [`LICENSE`](LICENSE).
