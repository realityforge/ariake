# Static Content Example Requirements

Status: review requested; implementation proceeding under the user's feature request.

## Mission

Add a second Ariake example application that demonstrates:

- serving static files from a filesystem directory;
- cache-control behavior ported from Rose's `CacheControlFilter`;
- pre-encoded Brotli sidecar behavior ported from Rose's `PreEncodedBrotliFilter`;
- Bazel automation that creates `.br` variants from source static files during the build;
- a JPA entity persisted by a lightweight database.

## Scope Boundaries

- Create a new example app instead of expanding `examples/hello`.
- Keep framework changes minimal; prefer example-owned services and filters.
- Preserve Ariake's per-directory Bazel ownership rules.
- Do not use `glob()` in Bazel targets.
- Do not list files from child, sibling, or parent directories in Bazel targets.

## Locked Decisions

- Example package: `examples/static` with Java package `org.ariake.examples.staticcontent`.
- Static route prefix: `/static`.
- Static root default: Bazel runfiles path for `examples/static/content`, with a source-tree fallback for direct execution.
- Cache-control behavior:
  - paths containing `.nocache.` receive no-cache headers;
  - paths containing `.cache.` receive one-year immutable cache headers;
  - all other static files receive no cache headers from the filter.
- Brotli behavior:
  - if `Accept-Encoding` includes `br`, the requested static source file exists, and a `.br` sidecar exists, serve the sidecar with `Content-Encoding: br`;
  - do not serve `.br` sidecars directly via the rewritten path guard;
  - set the content type from the original path.
- Database: H2 `2.4.240`, verified from Maven Central metadata on 2026-06-24.
- JPA provider: existing EclipseLink dependency and `EclipseLinkEntityManagerProvider`.

## Behavior Expectations

- `bazel build //examples/static:server` creates Brotli sidecar outputs for explicitly listed text assets.
- `bazel run //examples/static:server` starts the static example on the configured port.
- `GET /static/index.nocache.html` serves static content with no-cache headers.
- `GET /static/app.cache.js` serves static content with immutable cache headers.
- `GET /static/app.cache.js` with `Accept-Encoding: br` serves the generated `.br` sidecar.
- The example persists a JPA `PageView` entity when static files are served and exposes a small JSON summary endpoint.

## Quality Gates

- Targeted gates during implementation:
  - `bazel build //examples/static:server`
  - focused `bazel test` targets added for example behavior, if tests are added.
- Required full gate before completion:
  - `tools/check.sh`

## Known Intentional Divergences

- The Rose filters are Servlet filters; the Ariake example ports the behavior to `HttpFilter`.
- Static content serving is example-owned rather than a new framework-level static resource API.
- The Brotli build action uses the local `brotli` CLI because `brotli 1.1.0` is installed on this machine and the user asked for build-time encoding.

## Open Questions Register

| id | status | question | context | options | tradeoffs | recommended_default | user_decision | artifacts_updated |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Q-01 | resolved | Should this be a new example or an extension of `examples/hello`? | The user asked for "another example" and the repo currently has only `examples/hello`. | New example; extend hello. | A new example keeps feature scope isolated; extending hello is less discoverable. | New example. | Use a new `examples/static` app. | `00-requirements.md`, `10-implementation-plan.md`, `20-task-board.yaml` |
| Q-02 | resolved | Which lightweight database should back the JPA entity? | Existing Ariake JPA support uses EclipseLink but no JDBC database dependency is present. | H2 file database; H2 in-memory database. | File mode demonstrates persistence across restarts; in-memory is simpler but less visibly persistent. | H2 file database. | Use H2 file database with a configurable path. | `00-requirements.md`, `10-implementation-plan.md`, `20-task-board.yaml` |
| Q-03 | resolved | How should Brotli variants be generated? | The source static files should remain present while the build creates encoded sidecars. | Explicit Bazel `genrule` actions using `brotli`; checked-in `.br` files. | Generated sidecars prove build automation; checked-in sidecars do not. | Explicit Bazel generation. | Generate `.br` outputs in Bazel from explicitly listed source files. | `00-requirements.md`, `10-implementation-plan.md`, `20-task-board.yaml` |
