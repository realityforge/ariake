# Ariake Research Report

## Report 1 - Baseline Decisions

Date: 2026-06-23

### Local Toolchain

- `java -version`: Java 17.0.12 LTS.
- `javac -version`: 17.0.12.
- `bazel --version`: 9.1.1.
- `bazelisk version`: Bazelisk 1.27.0 resolving Bazel 9.1.1.
- `bazel-depgen --help`: not installed on PATH.
- `native-image --version`: not installed on PATH.
- `bazel query '@rules_java//toolchains:all'`: shows `remotejdk_25` and `toolchain_jdk_25`; no `remotejdk_26` target in `rules_java` 9.6.1.

### Current Upstream Versions

- Bazel: GitHub release page reports 9.1.1 as latest, released 2026-06-03.
- bazel-depgen: Maven Central metadata reports 0.24.
- Helidon SE: Maven metadata reports 4.5.0 for webserver, websocket, webserver-http2, webclient, and http artifacts.
- Sting: Maven metadata reports 0.39 for `sting-core`, `sting-processor`, and `sting-server`.
- Prometheus Java metrics: Maven metadata reports 1.8.0 for `prometheus-metrics-core` and `prometheus-metrics-exposition-formats`.
- Narayana JTA: Maven metadata reports 7.3.4.Final.
- EclipseLink JPA: Maven metadata reports `5.0.1-RC2` as latest and 5.0.0 as latest stable visible in the stream.
- JUnit 4: Maven metadata reports 4.13.2.
- ArchUnit JUnit4: Maven metadata reports 1.4.2.
- Java: OpenJDK reports JDK 26 GA on 2026-03-17; Oracle release notes report JDK 26.0.1 GA on 2026-04-21; JDK 27 is early-access/in-development.

### API Notes

- Helidon v4 WebServer uses `WebServer.builder().routing(it -> it.get(...)).build()`.
- Helidon v4 WebSocket support uses `WsRouting.builder().endpoint(path, WsListener)` and requires `helidon-webserver-websocket`.
- Helidon v4 protocol-specific HTTP/2 routes require an extra HTTP/2 dependency.
- Sting generates package-private `Sting_*` injector implementations from `@Injector` interfaces; common usage adds a static `create()` method that calls the generated class.

### Build Notes

- Rose uses Bzlmod and `bazel-depgen` generated content embedded into `MODULE.bazel` and `third_party/java/BUILD.bazel`.
- `bazel-depgen init` failed outside a Bazel workspace with a null cache-path exception, so Ariake should run `generate` from inside the repo with an explicit cache directory.
- The generated dependency labels should come from `bazel-depgen`, not from hand-written guesses.
- Default Bazel Java builds should use `remotejdk_25` until `rules_java` publishes a JDK 26 remote toolchain or Ariake adds a custom one.

### Risks To Verify

- Exact generated labels for Helidon, Sting, Narayana, Prometheus, and EclipseLink.
- Bazel 9 plus generated repository rules compatibility.
- Sting annotation processor plugin target naming.
- Helidon 4.5.0 WebSocket imports and method signatures.

## Report 2 - Implementation And Formatting Verification

Date: 2026-06-23

### Framework Status

- Ariake now has framework-owned APIs for config, HTTP, WebSocket, metrics, transactions, JPA, and server lifecycle.
- Helidon SE is isolated under `org.ariake.server.helidon`.
- Prometheus metrics are isolated behind `org.ariake.metrics`.
- Narayana transactions are isolated behind `org.ariake.tx`.
- EclipseLink JPA bootstrap is isolated behind `org.ariake.jpa`.
- The `examples/hello` app uses Sting to inject properties config, metrics, transaction runner, services, and the server.

### Palantir Java Format

- Maven Central metadata reports `com.palantir.javaformat:palantir-java-format` latest/release as 2.93.0.
- The formatter is declared in `third_party/java/dependencies.yml` and generated into Bazel through `tools/update_java_deps.sh`.
- Palantir's POM includes an optional runtime dependency on `com.palantir.javaformat:palantir-java-format-parent` as a jar, but Maven Central provides that coordinate as a parent POM rather than a jar. The dependency is excluded in depgen config.
- The Palantir transitive graph includes Guava's `listenablefuture` empty marker, which has no sources jar. Ariake declares it explicitly with `includeSource: false`.
- `bazel run //tools/java-format:palantir_java_format -- --help` reports Palantir Java Format 2.93.0 and confirms `--replace`, `--dry-run`, and `--set-exit-if-changed`.
- Running the formatter on Java 25 requires javac module exports. The Bazel binary supplies the same javac package exports documented for google-java-format on JDK 16+.

### Verification Evidence

- `tools/update_java_deps.sh`: passed after the Palantir parent exclusion and Guava marker declaration.
- `tools/java_format.sh write`: passed and formatted repository Java sources in Palantir style.
- `tools/java_format.sh check`: passed with no formatting drift.
- `tools/check.sh`: passed after adding formatter enforcement. The gate ran dependency generation, Buildifier check, Palantir Java Format check, `bazel build //...`, and `bazel test //...`.
- `bazel test //...` executed 6 test targets and all 6 passed.

### Residual Risks

- Native-image support remains an opt-in hook because local `native-image` is not installed.
- Java 26 remains a documented target direction, but the default Bazel remote toolchain remains Java 25 until a JDK 26 Bazel toolchain is added or published upstream.

## Report 3 - NullAway Enforcement

Date: 2026-06-23

### Source-Backed Decisions

- Maven Central metadata reports `com.uber.nullaway:nullaway` latest/release as 0.13.7.
- Maven Central metadata reports `org.jspecify:jspecify` latest/release as 1.0.0.
- NullAway's README says `-Xep:NullAway:ERROR` is the Error Prone flag equivalent for treating NullAway findings as errors.
- NullAway's configuration docs say version 0.12.3 and later supports `-XepOpt:NullAway:OnlyNullMarked=true`.
- NullAway's JSpecify docs say `RequireExplicitNullMarking` checks that every class is explicitly `@NullMarked` or `@NullUnmarked` via class, package, or module annotations.

### Implementation

- `third_party/java/dependencies.yml` now includes NullAway 0.13.7 and JSpecify 1.0.0.
- `third_party/java/BUILD.bazel` exposes a `nullaway_plugin` using the generated `:nullaway` target.
- `third_party/java/rules.bzl` promotes `NullAway` and `RequireExplicitNullMarking` to `ERROR` and passes `OnlyNullMarked=true`.
- The Java wrapper adds JSpecify as a direct dependency only for source-bearing targets, avoiding invalid deps on source-less aggregate libraries.
- Every Ariake source and test package with Java classes now has a JSpecify `@NullMarked` `package-info.java`.
- The previous `TransactionRunner` null-return issue was resolved, and the programmatic runner API was later removed after Sting server transactions became the preferred boundary.

### Verification Evidence

- `bazel build //src/main/java/org/ariake:config` failed before package markers with `RequireExplicitNullMarking`, proving the checker is active as an error.
- `bazel build //...` passed after package markers and the transaction API correction.
- `bazel test //...` passed with all 6 tests green.
- `tools/check.sh` passed with dependency generation, Buildifier, Palantir Java Format, NullAway-enabled build, and all tests.

## Report 4 - Sting 0.39 Server Transaction Interceptors

Date: 2026-06-23

### Source-Backed Decisions

- Maven metadata reports `org.realityforge.sting:sting-core` 0.39 as latest/release.
- Maven metadata reports `org.realityforge.sting:sting-processor` 0.39 as latest/release.
- Maven metadata reports `org.realityforge.sting:sting-server` 0.39 as latest/release.
- The `sting-server` 0.39 POM depends on `jakarta.transaction:jakarta.transaction-api:2.0.1`.
- Sting interceptor docs state that interception applies at the service-interface boundary through generated proxies and does not intercept direct self-invocation.
- Sting typing docs state that `@Typed` is required when an injectable implementation should publish a service interface instead of only its concrete type.

### Implementation

- `third_party/java/dependencies.yml` now uses Sting core/processor 0.39 and includes `sting-server` 0.39.
- `HealthResponder` is a Sting-published service interface annotated with `sting.server.Transactional`.
- `HealthResponderImpl` is `@Injectable` and `@Typed(HealthResponder.class)`, so Sting can provide the transactional proxy.
- `HealthService` remains the HTTP route adapter and calls `HealthResponder.health()` per request.
- `HelloFragment` provides Narayana's `jakarta.transaction.TransactionManager` to satisfy the Sting server transaction interceptors.
- `HealthResponderImpl` verifies `Status.STATUS_ACTIVE` before returning `OK`, so the runtime smoke would fail if the Sting transaction proxy were not active.

### Verification Evidence

- `tools/update_java_deps.sh`: passed after upgrading Sting and adding `sting-server`.
- `bazel build //examples/hello:server`: passed after including `HealthResponderImpl` in the injector.
- `tools/check.sh`: passed with dependency generation, Buildifier, Palantir Java Format, NullAway-enabled build, and all tests.
- Runtime smoke on port 63779:
  - `GET /health` returned `HTTP/1.1 200 OK` with body `OK`.
  - `GET /metrics` returned `ariake_health_requests_total 1.0`.

### Design Note

- Sting transaction interceptors are now the preferred high-level request-time transaction boundary for application services.
- Ariake no longer exposes a separate `TransactionRunner`; Sting server `@Transactional` is the transaction boundary for application services.
- The Narayana adapter remains only as the `jakarta.transaction.TransactionManager` provider required by Sting server interceptors.

## Report 5 - Programmatic Transaction Runner Removal

Date: 2026-06-23

### Decision

- `TransactionRunner`, `TransactionalWork`, and `TransactionalRunnable` were removed because Sting server `@Transactional` now provides the application transaction boundary.
- Keeping both models would create duplicate transaction semantics and additional API surface without a current caller.
- Narayana integration now exposes only `NarayanaTransactions.transactionManager()`.

### Verification Evidence

- `rg -n "TransactionRunner|TransactionalWork|TransactionalRunnable" src examples` reports no production, example, or test references.
- `NarayanaTransactionsTest` verifies the remaining Narayana transaction manager provider can start an active transaction.

## Report 6 - Bazel Source Directory Ownership

Date: 2026-06-24

### Finding

- Existing `src/main/java/org/ariake/BUILD.bazel`, `src/test/java/org/ariake/BUILD.bazel`, and `examples/hello/BUILD.bazel` listed Java files from nested source directories.
- Bazel package boundaries prevent a parent package from owning files below a child directory once that child directory has its own `BUILD.bazel`.
- The clean enforcement mechanism is therefore to add a `BUILD.bazel` to each Java source directory and leave parent packages as source-less aggregators.

### Verification Evidence

- `rg -n "glob\\(" . --glob 'BUILD.bazel' --glob '*.bzl'` reported no matches.
- `rg -n '"[^"]*/.*\\.java"' src examples --glob 'BUILD.bazel'` reported no matches.
- A source-directory scan verified every directory containing Java source under `src/main/java`, `src/test/java`, and the hello example has a local `BUILD.bazel`.
- `tools/check.sh` passed with dependency generation verification, Buildifier check, Palantir Java Format check, `bazel build //...`, and 6/6 tests.

## Report 7 - Rose HTTP Filter Requirements

Date: 2026-06-24

### Rose Behavior Evidence

- `CacheControlFilter` is scoped to `/rose/*`, reads `HttpServletRequest.getRequestURI()`, sets `Date`, `Last-Modified`, `Expires`, `Pragma`, and `Cache-control`, then always continues the servlet chain.
- `PreEncodedBrotliFilter` is scoped to `/rose/*`, reads `Accept-Encoding`, checks normal and `.br` static resources, sets `Content-Encoding` and original `Content-Type`, includes the encoded resource, and short-circuits when it serves content.
- `RoseApiClientKeycloakFilter` has ordered URL patterns for API paths, supports config-driven bypass, delegates to Keycloak before chain continuation, and lets `RoseApiClientKeycloakUrlFilterImpl` short-circuit with `403` after checking the authenticated user.
- `RoseApiClientKeycloakUrlFilterImpl` invalidates a servlet session on post-auth failure, but Ariake should not expose servlet sessions in the core API; Ariake auth integrations should model stateless security context instead.

### Ariake API Direction

- Add Ariake-owned `HttpFilter` and `HttpFilterChain` interfaces plus immutable filter registrations on `HttpRoutes`.
- Extend `HttpExchange` with method, request URI, request header lookup, date headers, error sends, and send-state inspection.
- Keep static resource lookup as a later `HttpResources`-style abstraction rather than adding resource operations to `HttpExchange`.
- Keep all Helidon types isolated in `org.ariake.server.helidon`; public `org.ariake.http` APIs must remain implementation-free.

### Implementation Verification

- `bazel test //src/test/java/org/ariake/http:http_test`: passed with coverage for filter ordering, path matching, unsupported patterns, short-circuiting, and exchange helper usage.
- `bazel build //src/main/java/org/ariake/server/helidon:helidon_server //examples/hello:server`: passed, confirming the Helidon adapter and example compile against the extended exchange/filter API.
- `tools/java_format.sh check`: passed after formatting the Java changes.
- `rg -n "glob\\(" . --glob 'BUILD.bazel' --glob '*.bzl'`: no matches.
- `rg -n '"[^"]*/.*\\.java"' src examples --glob 'BUILD.bazel'`: no matches.
- `tools/check.sh`: passed with dependency verification, Buildifier, Palantir Java Format, `bazel build //...`, and 6/6 tests.
