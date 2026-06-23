# Ariake Framework Requirements

## Mission

Create Ariake, a lightly coupled Java server-side framework for fast-starting HTTP, HTTP/2, and WebSocket services. Ariake should expose small framework-owned abstractions while using Helidon SE, Sting, Narayana, Prometheus Java metrics, EclipseLink JPA, and Bazel behind clear module boundaries.

## Scope Boundaries

- In scope:
  - Greenfield Bazel 9 Java repository scaffold.
  - `bazel-depgen` managed Maven dependencies under `third_party/java`.
  - Core framework APIs for configuration, HTTP services, WebSocket services, metrics, transaction manager integration, and JPA bootstrap.
  - Helidon SE runtime adapter that can serve HTTP and WebSocket endpoints and includes HTTP/2 support on the classpath.
  - Properties-file configuration loaded once and provided through Sting.
  - Prometheus metrics endpoint using `io.prometheus:prometheus-metrics-core:1.8.0`.
  - Narayana-backed `TransactionManager` provider for Sting server interceptors.
  - EclipseLink-backed JPA helper.
  - Example Ariake application demonstrating health, metrics, and WebSocket echo services.
  - Unit tests and architecture checks.
  - Automated Java source formatting through Palantir Java Format.
  - NullAway nullness checking with all warnings promoted to errors.
  - Optional GraalVM native-image documentation/build script hooks.
- Out of scope for the initial foundation:
  - Full MVC/controller framework.
  - Authentication/authorization.
  - Database migrations.
  - Production TLS certificate management.
  - HTTP/2 protocol conformance tests beyond ensuring the dependency/build/runtime support is present.

## Locked Decisions

- Framework name: Ariake.
- Java baseline: Java 25 for the default Bazel build because `rules_java` 9.6.1 exposes `remotejdk_25`, the latest verified Bazel-provided remote JDK in this environment.
- Java 26 target: Java 26 is the current stable GA Java line as of 2026-06-23, but `rules_java` 9.6.1 does not expose `remotejdk_26`; add a custom Bazel JDK 26 toolchain when the framework needs Java 26 language/API features before upstream `rules_java` publishes one.
- Build tool: Bazel 9.1.1, pinned in `.bazelversion`.
- Dependency generation: `bazel-depgen` 0.24, configured through `third_party/java/dependencies.yml`.
- Dependency injection: Sting 0.39 with `sting-processor` wired as a Bazel annotation processor.
- Sting server transactions: `org.realityforge.sting:sting-server:0.39` provides compile-time service-interface transaction interceptors using `jakarta.transaction`.
- HTTP/WebSocket runtime: Helidon SE 4.5.0.
- HTTP/2 support: include `io.helidon.webserver:helidon-webserver-http2:4.5.0`.
- Metrics: Prometheus Java client `io.prometheus:prometheus-metrics-core:1.8.0` plus exposition formatting from the same release for `/metrics`.
- Transactions: Narayana JTA 7.3.4.Final, exposed to Sting server interceptors through a `jakarta.transaction.TransactionManager` service; no separate programmatic transaction runner API is exposed.
- JPA: EclipseLink 5.0.0 stable. Maven metadata reports `5.0.1-RC2` as latest, but the requirement says modern EclipseLink, not release candidate.
- Configuration format: Java `.properties` file, loaded by framework code and injected through Sting.
- Java source formatting: Palantir Java Format 2.93.0, invoked in Palantir style through a Bazel binary.
- Nullness: NullAway 0.13.7 runs as an Error Prone plugin for all source-bearing Java targets, with `NullAway` and `RequireExplicitNullMarking` set to `ERROR`.
- Nullness marking model: JSpecify 1.0.0 `@NullMarked` package defaults; nullable/null-bearing APIs must be marked explicitly with JSpecify `@Nullable`.

## Command Surface

- `bazel build //...`: build all Java targets and tools.
- `bazel test //...`: run unit and architecture tests.
- `bazel run //examples/hello:server -- --config=examples/hello/application.properties`: start the example server.
- `tools/update_java_deps.sh`: download/run `bazel-depgen` and refresh generated Java dependency targets from `third_party/java/dependencies.yml`.
- `tools/check.sh`: run dependency generation verification, build, tests, and formatting/architecture checks.
- `tools/java_format.sh write`: format all repository Java source files with Palantir Java Format.
- `tools/java_format.sh check`: fail if any repository Java source file is not Palantir Java Format compliant.
- `tools/native-image.sh`: optional GraalVM native-image build hook; fails with a clear message when `native-image` is not installed.

## Behavior Expectations

- Framework consumers should implement small Ariake service interfaces rather than depending directly on Helidon.
- Runtime adapters may depend on Helidon, Narayana, Prometheus, and EclipseLink.
- Configuration is immutable after loading.
- Application transaction boundaries should use Sting server `@Transactional` service-interface proxies.
- Narayana-specific integration should stay in the Narayana adapter package.
- Transactional service boundaries may be declared on Sting-published service interfaces using Sting server's `@Transactional`.
- Metrics registry should be injectable and expose Prometheus text format for a Helidon route.
- Server startup should avoid classpath scanning and runtime reflection where practical; Sting compile-time DI is the default bootstrap path.
- The example server should expose:
  - `GET /health` returning `OK`.
  - `GET /metrics` returning Prometheus text exposition.
  - WebSocket `/ws/echo` echoing text messages.

## Quality Gates

- `bazel build //...` must pass.
- `bazel test //...` must pass.
- `tools/check.sh` must pass or document a missing local optional tool.
- `tools/java_format.sh check` must pass and is run automatically by `tools/check.sh`.
- NullAway warnings must fail compilation as errors.
- Every source-bearing Java package must be explicitly `@NullMarked` or `@NullUnmarked`; Ariake code uses `@NullMarked` by default.
- `bazel-depgen` generation must be reproducible from `third_party/java/dependencies.yml`.
- Architecture tests must enforce:
  - Public framework APIs do not import Helidon, Narayana, EclipseLink, or Prometheus implementation classes except in adapter packages.
  - Example code may depend on Ariake APIs but core framework code must not depend on example packages.
  - Configuration remains in the config package and is provided to runtime through Sting.

## Research Evidence

- Bazel release page reports 9.1.1 as latest release dated 2026-06-03: https://github.com/bazelbuild/bazel/releases
- OpenJDK reports JDK 26 reached General Availability on 2026-03-17, while JDK 27 is still in development: https://openjdk.org/projects/jdk/26/
- Oracle release notes report JDK 26.0.1 GA on 2026-04-21: https://www.oracle.com/java/technologies/javase/26all-relnotes.html
- Local `bazel --version` returned `bazel 9.1.1`.
- Helidon Maven metadata reports 4.5.0 for `helidon-webserver`, `helidon-webserver-websocket`, and `helidon-webserver-http2`: https://repo.maven.apache.org/maven2/io/helidon/webserver/
- Helidon v4 WebServer docs show the builder/routing API and state HTTP/2 routes need an additional dependency: https://helidon.io/docs/v4/se/webserver/webserver
- Helidon v4 WebSocket docs show `WsRouting.builder().endpoint(...)` with `WsListener`: https://helidon.io/docs/v4/se/websocket
- Sting Maven metadata reports `sting-core` and `sting-processor` 0.39: https://repo.maven.apache.org/maven2/org/realityforge/sting/
- Sting server Maven metadata reports `sting-server` 0.39: https://repo.maven.apache.org/maven2/org/realityforge/sting/sting-server/maven-metadata.xml
- Sting server 0.39 POM depends on `jakarta.transaction-api` 2.0.1: https://repo.maven.apache.org/maven2/org/realityforge/sting/sting-server/0.39/sting-server-0.39.pom
- Sting docs show `@Injectable`, `@Injector`, generated `Sting_*` implementations, and annotation processor setup: https://sting-ioc.github.io/docs/getting_started.html
- Sting interceptor docs state interception applies at the service-interface boundary through generated proxies: https://github.com/sting-ioc/sting/blob/master/docs/interceptors.md
- Narayana Maven metadata reports `narayana-jta` 7.3.4.Final: https://repo.maven.apache.org/maven2/org/jboss/narayana/jta/narayana-jta/maven-metadata.xml
- Prometheus Maven metadata reports `prometheus-metrics-core` 1.8.0: https://repo.maven.apache.org/maven2/io/prometheus/prometheus-metrics-core/maven-metadata.xml
- EclipseLink Maven metadata reports stable 5.0.0 and RC 5.0.1-RC2: https://repo.maven.apache.org/maven2/org/eclipse/persistence/org.eclipse.persistence.jpa/maven-metadata.xml
- `bazel-depgen` README documents Maven coordinate `org.realityforge.bazel.depgen:bazel-depgen:0.24:all`: https://github.com/realityforge/bazel-depgen
- Maven Central metadata reports `com.palantir.javaformat:palantir-java-format` 2.93.0 as latest/release: https://repo.maven.apache.org/maven2/com/palantir/javaformat/palantir-java-format/maven-metadata.xml
- Palantir Java Format CLI reports version 2.93.0 and supports `--replace`, `--dry-run`, and `--set-exit-if-changed`.
- Palantir Java Format inherits javac-internal access requirements from google-java-format on JDK 16+; the formatter Bazel binary supplies the required `--add-exports` JVM flags: https://github.com/palantir/palantir-java-format/issues/548
- Maven Central metadata reports `com.uber.nullaway:nullaway` 0.13.7 as latest/release: https://repo.maven.apache.org/maven2/com/uber/nullaway/nullaway/maven-metadata.xml
- Maven Central metadata reports `org.jspecify:jspecify` 1.0.0 as latest/release: https://repo.maven.apache.org/maven2/org/jspecify/jspecify/maven-metadata.xml
- NullAway docs state `-Xep:NullAway:ERROR` promotes NullAway findings to errors and `OnlyNullMarked=true` enables `@NullMarked`-only checking: https://github.com/uber/NullAway/wiki/Configuration
- NullAway docs state `RequireExplicitNullMarking` checks that every class is explicitly `@NullMarked` or `@NullUnmarked`: https://github.com/uber/NullAway/wiki/JSpecify-Support

## Intentional Divergences

- EclipseLink uses 5.0.0 rather than 5.0.1-RC2 to avoid pinning the initial framework to a release candidate.
- `native-image` support is optional because `native-image` is not installed on the verified local PATH.
- The build uses Java 25 rather than Java 26 until a Bazel JDK 26 toolchain is added or published by `rules_java`; this is a build-toolchain constraint, not a product preference.
- JUnit 4.13.2 is used for Bazel-native Java tests because Bazel's standard Java test runner supports JUnit 4 directly; JUnit 5 can be added later with an explicit console runner target if required.
- Palantir Java Format excludes the optional `com.palantir.javaformat:palantir-java-format-parent` jar from depgen resolution because Maven Central publishes that artifact as a parent POM, not a runtime jar.
- Guava's empty `listenablefuture` marker is declared explicitly with `includeSource: false` because the marker artifact has no sources jar.
- NullAway uses `OnlyNullMarked=true` instead of `AnnotatedPackages=org.ariake` so package-level JSpecify markings are the single source of nullness scope.

## Open Questions Register

No blocking open questions. Defaults above are derived from the user's stack requirements, current upstream metadata, and the verified local toolchain.
