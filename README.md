# simpsonm09-repo-template

A layered Kotlin service on Java 25 and Spring Boot 4, wired to the shared `repo-standard`. It
ships one feature, item CRUD over HTTP, as a worked example of the fleet's layering and tooling.

Read the [documentation index](docs/README.md) for the architecture, the feature pages, and the
generated OpenAPI document.

## What is included

- A layered package under `src/main/kotlin`: `api`, `service`, `domain`, `persistence`, `config`, and `exception`.
- Spring Web MVC, bean validation, Spring Data JPA over H2, and Actuator, with springdoc generating the OpenAPI document from the code.
- Unit tests for the service and the controller, an integration test over the real endpoints, and a test that writes `docs/openapi.json`.
- JaCoCo coverage converted to lcov by `scripts/jacoco-to-lcov.mjs`. The shared CI coverage workflow runs the pinned standard's patch gate: 80 percent of changed lines and 70 percent of changed branches.
- A CI caller that runs the shared lint, aislop, security, standard, and coverage jobs plus this repository's `test` job.
- A `justfile` task runner with `just` and `gradle` pinned in `mise.toml`.
- A multi-stage `Dockerfile` and a `compose.yaml` for local runs.

## Local commands

```bash
just install    # install the pinned tools with mise
just deps       # resolve the Gradle dependencies
just test       # run the JVM test suite
just coverage   # run the tests and write coverage/lcov.info
just spec       # regenerate docs/openapi.json from the code
just lint       # run flint
just complexity # check Kotlin complexity with Detekt
just verify     # lint, complexity, and test
```

The recipes run the pinned tools through `mise exec --`, so run `just install` once and then `just test` as shown. The `coverage` recipe also runs `node`. `mise.toml` does not pin Node, so `coverage` uses the Node on PATH.

## Layout

- `src/main/kotlin/com/simpsonm09/template/` holds the application, split into layers.
- `src/test/kotlin/com/simpsonm09/template/` holds the unit, integration, and document tests.
- `docs/` holds the documentation tree; [docs/README.md](docs/README.md) is the index.
- `justfile` is the one local entry point for a human or an agent.
- `scripts/` holds the coverage converter and the shared prune script. The shared coverage workflow checks out the pinned standard for its patch-coverage script.
- `.env.example` describes the optional local file; root `.worktreeinclude` lists `.env` for worktree copying. See [Repository setup](docs/features/repository-setup.md).
- `.github/workflows/ci.yml` is the caller for the shared workflows plus the test job.

## Starting a repository from this template

The decisions a new repository owner makes on day one are in [Repository setup](docs/features/repository-setup.md#decide-on-day-one).

## License

MIT. See [`LICENSE`](LICENSE).
