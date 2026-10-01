# repo-template working agreements

A generated-repo starting point. It is a working Python project with a placeholder package, wired to the shared standard.

## Ground rules

- This repository is a GitHub template. A change here reaches every new repository, so keep it generic and free of repo-specific facts.
- The placeholder package is `src/app`. Do not add domain logic.
- Keep the README, `AGENTS.md`, and the files under `templates/` as skeletons with clear placeholders.
- No secret, credential, or machine path is committed.

## Commands

- `just install`, `just lint`, `just test`, `just verify`.

## Repo facts

- Language and toolchain: Python 3.12, pinned in `mise.toml` and `pyproject.toml`.
- `templates/` holds the OpenCode agent and skill skeletons copied into a new repository.
- After a repository is created from this template, replace the README, rename the `app` package, and run `flint init`.

## Skills

No repo-local skills. General best practices and integration come from the plugins.
