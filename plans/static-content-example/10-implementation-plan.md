# Static Content Example Implementation Plan

Status: review requested; implementation proceeding.

## Phase Sequence

1. Planning and dependency orientation.
2. Static example structure and build-time Brotli sidecars.
3. Example-owned static handler plus cache-control and Brotli filters.
4. JPA entity, repository/service endpoint, H2 dependency, and persistence resources.
5. Validation with targeted checks and `tools/check.sh`.

## Delivery Approach

- Add `examples/static` without modifying `examples/hello`.
- Keep all Java sources in one example source directory with its own `BUILD.bazel`.
- Add resource and static-content BUILD files in the directories that own those files.
- Use explicit `genrule` targets for each Brotli-generated sidecar.
- Use `EclipseLinkEntityManagerProvider.create(String, Map<String, ?>)` with H2 JDBC properties supplied by the example fragment.
- Commit planning first, then implementation in logical behavior blocks.

## High-Risk Areas

- Bazel action access to `brotli`: mitigate by running `bazel build //examples/static:server` and full `tools/check.sh`.
- Helidon route pattern for nested static paths: mitigate with targeted runtime or build checks.
- JPA resource packaging: mitigate by building the example and exercising persistence if practical.
- Generated dependency files: update `third_party/java/dependencies.yml` first and regenerate `MODULE.bazel` / `third_party/java/BUILD.bazel` with `tools/update_java_deps.sh`.

## Required Full Gate

```bash
tools/check.sh
```

## Decision Log

- Q-01: Implement a new `examples/static` app to keep the feature demonstrable and isolated.
- Q-02: Use an H2 file database because it is lightweight and persists across server restarts.
- Q-03: Use explicit Bazel Brotli generation so source files stay unencoded and `.br` variants are build outputs.

## Expected Files

- `examples/static/BUILD.bazel`
- `examples/static/application.properties`
- `examples/static/content/BUILD.bazel`
- `examples/static/content/*`
- `examples/static/src/main/java/org/ariake/examples/staticcontent/BUILD.bazel`
- `examples/static/src/main/java/org/ariake/examples/staticcontent/*.java`
- `examples/static/src/main/resources/META-INF/BUILD.bazel`
- `examples/static/src/main/resources/META-INF/persistence.xml`
- `third_party/java/dependencies.yml`
- generated dependency files from `tools/update_java_deps.sh`
