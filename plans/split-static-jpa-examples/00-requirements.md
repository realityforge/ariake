# Split Static and JPA Examples Requirements

Status: review requested; implementation proceeding.

## Mission

Separate the current combined `examples/static` application into:

- a static-content example focused on directory-backed static files, cache-control filters, Brotli sidecars, and build-time Brotli generation;
- a JPA example focused on a persisted entity backed by a lightweight database.

Add integration coverage that starts compiled example jars as external Java processes and verifies behavior through HTTP.

## Scope Boundaries

- Keep `examples/static` free of JPA, H2, EclipseLink, and persistence resources.
- Add a new `examples/jpa` app with its own Java source directory and `BUILD.bazel`.
- Preserve explicit Bazel source lists; do not use `glob()`.
- Preserve every Java source directory owning its own `BUILD.bazel`.
- Keep the integration test under `src/test/java` and run it through `tools/check.sh`.

## Locked Decisions

- Static example keeps `/static` endpoints and Brotli/cache behavior.
- JPA example uses package `org.ariake.examples.jpa` and exposes `/page-views`.
- JPA example continues to use H2 and `EclipseLinkEntityManagerProvider`.
- Integration tests launch Bazel deploy jars with `java -jar`.
- Integration tests use `ariake.server.port=0` and parse the actual port from stdout.

## Behavior Expectations

- `//examples/static:server` serves static content without recording page views.
- `//examples/jpa:server` persists `PageView` entities independently of static content.
- The integration test verifies:
  - static no-cache and cache/Brotli headers from the static jar;
  - JPA count and create behavior from the JPA jar.

## Quality Gates

- Targeted:
  - `bazel build //examples/static:server //examples/jpa:server`
  - `bazel test //src/test/java/org/ariake/examples:examples_integration_test`
- Required full gate:
  - `tools/check.sh`

## Open Questions Register

| id | status | question | context | options | tradeoffs | recommended_default | user_decision | artifacts_updated |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Q-01 | resolved | Should integration coverage launch source-tree classes or compiled jars? | The user explicitly requested starting the program using the compiled jar. | Compiled deploy jar; classpath run from Bazel test. | Deploy jar most directly validates the user request; classpath launch is less representative. | Compiled deploy jar. | Use Bazel deploy jars with `java -jar`. | `00-requirements.md`, `10-implementation-plan.md`, `20-task-board.yaml` |
