# simpsonm09-repo-template

A layered Kotlin service on Java 25 and Spring Boot 4, wired to the shared `repo-standard`. It
ships one feature, item CRUD over HTTP, as a worked example of the fleet's layering and tooling.

Read the [documentation index](docs/README.md) for the architecture, the feature pages, and the
generated OpenAPI document.

## What is included

- A layered package under `src/main/kotlin`: `api`, `service`, `domain`, `persistence`, `config`, and `exception`.
- Spring Web MVC, bean validation, Spring Data JPA over H2, and Actuator, with springdoc generating the OpenAPI document from the code.
- Unit tests for the service and the controller, an integration test over the real endpoints, and a test that writes `docs/openapi.json`.
- JaCoCo coverage converted to lcov by `scripts/jacoco-to-lcov.mjs` and gated by `scripts/patch-coverage.mjs`.
- A CI caller that runs the shared lint, aislop, security, and standard jobs plus this repository's `test` job.
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
just verify     # lint and test
```

## Layout

- `src/main/kotlin/com/simpsonm09/template/` holds the application, split into layers.
- `src/test/kotlin/com/simpsonm09/template/` holds the unit, integration, and document tests.
- `docs/` holds the documentation tree; [docs/README.md](docs/README.md) is the index.
- `justfile` is the one local entry point for a human or an agent.
- `scripts/` holds the coverage converter and the shared patch-coverage and prune scripts.
- `.github/workflows/ci.yml` is the caller for the shared workflows plus the test job.

## License

MIT. See [`LICENSE`](LICENSE).
