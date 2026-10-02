# Cross-platform task runner for humans and agents. `just --list` shows every
# recipe. Keep each recipe a thin call to a portable tool or a script under
# scripts/. See https://github.com/simpsonm09-org/simpsonm09-repo-standard/blob/main/docs/task-runner.md
set windows-shell := ["powershell.exe", "-NoLogo", "-NoProfile", "-Command"]

# List the recipes.
default:
    @just --list

# Install the pinned tools.
install:
    mise install

# Resolve and download the Gradle dependencies.
deps:
    gradle dependencies

# Run every linter over the tracked files.
lint:
    mise exec -- flint run --full

# Fix what the linters can fix.
lint-fix:
    mise exec -- flint run --fix

# Run the AI-slop gate.
aislop:
    npx --yes aislop@0.16.1 ci

# Run the test suite.
test:
    gradle test

# Run the tests with coverage and convert the JaCoCo report to lcov.
coverage:
    gradle test jacocoTestReport
    node scripts/jacoco-to-lcov.mjs

# Regenerate docs/openapi.json from the application.
spec:
    gradle generateOpenApi

# Lint and test.
verify: lint test

# Prune remote-tracking refs and delete local branches merged into main.
prune:
    node scripts/prune.mjs
