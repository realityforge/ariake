# Ariake Implementation Plan

## Plan Status

Working implementation plan. It is intentionally concrete enough to execute immediately while remaining updateable as verification exposes exact API or build constraints.

## Phase Sequence

1. Planning and research artifacts
   - Record source-backed dependency decisions.
   - Track tasks and quality gates.
2. Bazel and dependency management
   - Add `.bazelversion`, `.bazelrc`, `MODULE.bazel`, root `BUILD.bazel`, strict Java rule wrappers, and `third_party/java/dependencies.yml`.
   - Add `tools/update_java_deps.sh` to run `bazel-depgen` 0.24.
   - Generate `third_party/java/BUILD.bazel` and repository rules.
3. Framework API and adapters
   - Add config, HTTP, WebSocket, metrics, transaction, JPA, and runtime modules.
   - Keep public APIs thin and framework-owned.
4. Example application
   - Use Sting to inject properties, services, metrics, transaction manager, and server.
   - Use Sting server transaction interceptors at a service-interface boundary.
   - Provide health, metrics, and WebSocket echo endpoints.
5. Quality and architecture checks
   - Add unit tests and ArchUnit checks.
   - Add Palantir Java Format write/check automation.
   - Add NullAway with explicit JSpecify package nullness marking.
   - Add `tools/check.sh` full gate.
6. Verification and report
   - Run dependency generation, build, tests, and example startup smoke checks where possible.
   - Update task board evidence and research report.
7. HTTP filter pipeline slice
   - Add framework-owned filter and chain APIs in `org.ariake.http`.
   - Extend `HttpExchange` with request metadata, request header lookup, date headers, error sends, and send-state inspection.
   - Wire Helidon route dispatch through the Ariake filter chain while keeping Helidon types in the adapter package.
   - Add focused unit tests for filter registration, ordering, matching, and short-circuit semantics.

## Delivery Approach

- Execute one task at a time with minimal diffs.
- Prefer simple framework-owned interfaces over leaking implementation libraries.
- Use Sting compile-time injection for the example app and framework runtime composition.
- Keep GraalVM support as an opt-in target/script because local `native-image` is unavailable.
- Update planning artifacts whenever dependency versions, package boundaries, or gates change.
- Keep Bazel package ownership local to each Java source directory; parent packages aggregate labels only.
- Keep HTTP filter APIs servlet-free and Helidon-free; use framework-owned interfaces and records only.

## High-Risk Areas

- `bazel-depgen` output compatibility with Bazel 9/Bzlmod.
  - Impact: generated repository rules may need exact placement or labels.
  - Mitigation: run the published `bazel-depgen` jar in this repo and use generated output rather than hand-written labels.
- Helidon 4 API drift.
  - Impact: compile failures in server and WebSocket adapters.
  - Mitigation: build against Helidon 4.5.0, use official v4 docs as source, and inspect compiled jars if needed.
- Sting annotation processor wiring in Bazel.
  - Impact: generated injector classes may not compile.
  - Mitigation: expose `sting_processor_plugin` from generated targets and compile the example injector in the build.
- Sting server transaction interceptor boundary.
  - Impact: interceptors do not apply to direct self-invocation or route registration callbacks unless the request path calls a Sting-published service interface.
  - Mitigation: put the transactional boundary on a dedicated business service interface and call it from the HTTP route.
- Narayana/EclipseLink transitive dependency weight.
  - Impact: larger dependency graph and slower initial downloads.
  - Mitigation: isolate transaction and JPA adapters in separate libraries so minimal HTTP apps can avoid depending on them directly.
- GraalVM native-image compatibility.
  - Impact: Helidon/Narayana/EclipseLink may need reflect config for production native images.
  - Mitigation: provide opt-in native-image hook and document current local verification limits.
- Palantir Java Format on modern JDKs.
  - Impact: formatter execution fails unless javac internals are exported to the unnamed module.
  - Mitigation: add the documented `--add-exports` flags to the formatter Bazel binary and verify both write and check modes.
- NullAway as a default Java wrapper behavior.
  - Impact: source-less aggregate Java targets cannot accept implicit deps/plugins, and unmarked packages fail compilation.
  - Mitigation: apply NullAway/JSpecify only to source-bearing targets and add `@NullMarked` package-info files to all source/test packages.
- Cross-directory Bazel source references.
  - Impact: parent BUILD files can silently own child-package source files, weakening package boundaries.
  - Mitigation: place a `BUILD.bazel` in every Java source directory and keep parent targets source-less or label-only.
- HTTP filter chain semantics.
  - Impact: ambiguous ordering or path matching would make auth/cache/static-resource behavior unreliable.
  - Mitigation: define small ordered registration records, fail fast on unsupported path patterns, and cover proceed/short-circuit behavior with unit tests before relying on the Helidon adapter.

## Required Full Gates

`tools/check.sh`

## Decision Log

- D-01: Use Bazel 9.1.1 because GitHub release metadata and local Bazel both confirm it as current.
- D-02: Use Helidon 4.5.0 with explicit WebSocket and HTTP/2 artifacts because Maven metadata confirms 4.5.0 and Helidon docs call out HTTP/2 dependency requirements.
- D-03: Use Sting 0.39 because Maven metadata reports it as latest release and it publishes the Jakarta-compatible server module.
- D-04: Use EclipseLink 5.0.0 stable instead of 5.0.1-RC2 because the request asked for modern EclipseLink, not RC adoption.
- D-05: Use JUnit 4 with ArchUnit JUnit4 for initial Bazel tests because Bazel Java tests run JUnit 4 without an extra console runner.
- D-06: Java 26 is the latest stable GA line, but default Bazel builds use Java 25 because `rules_java` 9.6.1 exposes `remotejdk_25` and no `remotejdk_26`. A custom JDK 26 toolchain is the required follow-up if Ariake needs Java 26 APIs before upstream Bazel support lands.
- D-07: Use `com.palantir.javaformat:palantir-java-format:2.93.0` because Maven Central metadata reports it as the current release.
- D-08: Run Palantir Java Format through a Bazel `java_binary` with JDK module exports rather than a local PATH tool, so formatting is reproducible from Bazel-managed dependencies.
- D-09: Exclude Palantir's optional `palantir-java-format-parent` jar and explicitly declare Guava's no-source `listenablefuture` marker because those Maven metadata edge cases otherwise prevent `bazel-depgen` generation.
- D-10: Use NullAway 0.13.7 with JSpecify 1.0.0 because Maven Central metadata reports those as the current releases.
- D-11: Configure `-Xep:NullAway:ERROR`, `-Xep:RequireExplicitNullMarking:ERROR`, and `-XepOpt:NullAway:OnlyNullMarked=true` so nullable/null-bearing APIs require explicit marking and unmarked code fails compilation.
- D-12: Add JSpecify as an automatic direct dependency for source-bearing Java wrapper targets so `package-info.java` markings are compatible with strict Java deps.
- D-13: Use `org.realityforge.sting:sting-server:0.39` for transactional service-interface proxies because the published POM depends on `jakarta.transaction-api:2.0.1`.
- D-14: Remove the low-level `TransactionRunner` API after adopting Sting server `@Transactional`; Ariake now exposes only the Narayana `TransactionManager` provider needed by Sting server interceptors.
- D-15: Split Java BUILD ownership by source directory so source-bearing targets can only list files from their own Bazel package; parent BUILD files aggregate child labels only.
- D-16: Add Ariake-owned HTTP filters through `HttpRoutes` and keep static resource lookup out of `HttpExchange`; this covers cache and auth filter control flow now while leaving resource resolution as a dedicated future API.

## Completion Criteria

- All planned task-board tasks completed or explicitly deferred with evidence.
- `bazel-depgen` generated files are present and reproducible.
- `bazel build //...` passes.
- `bazel test //...` passes.
- `tools/check.sh` passes, except optional native-image absence is documented rather than treated as a full-gate failure.
- `tools/java_format.sh check` passes and is included in `tools/check.sh`.
- NullAway and explicit null marking are enforced by `bazel build //...` and therefore by `tools/check.sh`.
- Final report summarizes implementation, verification evidence, and residual risks.
