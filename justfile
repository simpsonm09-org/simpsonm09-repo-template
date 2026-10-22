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
    mise exec -- gradle dependencies

# Run every linter over the tracked files.
lint:
    mise exec -- flint run --full

# Fix what the linters can fix.
lint-fix:
    mise exec -- flint run --fix

# Run the Detekt complexity gate over the main Kotlin sources.
complexity:
    mise exec -- detekt --config detekt.yml --input src/main/kotlin

# Run the AI-slop gate.
aislop:
    npx --yes aislop@0.16.1 ci

# Run the test suite.
test:
    mise exec -- gradle test

# Run the offline tests for the Dependabot summary workflow.
test-workflow:
    mise exec -- node --test scripts/dependabot-summary.test.mjs

# Run the tests with coverage and convert the JaCoCo report to lcov.
coverage:
    mise exec -- gradle test jacocoTestReport
    mise exec -- node scripts/jacoco-to-lcov.mjs

# Regenerate docs/openapi.json from the application.
spec:
    mise exec -- gradle generateOpenApi

# Lint, check complexity, test, and test the workflow.
verify: lint complexity test test-workflow

# Prune remote-tracking refs and delete local branches merged into main.
prune:
    mise exec -- node scripts/prune.mjs
