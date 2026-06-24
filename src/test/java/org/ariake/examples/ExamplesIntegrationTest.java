package org.ariake.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public final class ExamplesIntegrationTest {
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(20);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Test
    public void staticDeployJarServesConfiguredDirectory() throws IOException, InterruptedException {
        final var tempDir = Files.createTempDirectory("ariake-static-example-test");
        try {
            final var staticRoot = tempDir.resolve("content");
            Files.createDirectories(staticRoot);
            Files.writeString(staticRoot.resolve("index.nocache.html"), "<!doctype html><p>static</p>");
            Files.writeString(staticRoot.resolve("app.cache.js"), "globalThis.staticLoaded = true;");
            Files.write(staticRoot.resolve("app.cache.js.br"), "encoded-js".getBytes(StandardCharsets.UTF_8));

            final var config = tempDir.resolve("application.properties");
            Files.writeString(
                    config,
                    "ariake.server.port=0\n"
                            + "ariake.server.shutdownHook=false\n"
                            + "static.example.staticRoot="
                            + staticRoot
                            + "\n");

            try (var server = ServerProcess.start(runfile("examples/static/server_deploy.jar"), config)) {
                final var index = get(server.uri("/static/index.nocache.html"));
                assertEquals(200, index.statusCode());
                assertEquals(
                        "no-cache, must-revalidate, pre-check=0, post-check=0",
                        index.headers().firstValue("Cache-Control").orElseThrow());

                final var cached = get(server.uri("/static/app.cache.js"));
                assertEquals(200, cached.statusCode());
                assertEquals(
                        "max-age=31536000, public, immutable",
                        cached.headers().firstValue("Cache-Control").orElseThrow());
                assertFalse(cached.headers().firstValue("Content-Encoding").isPresent());

                final var brotli = get(server.uri("/static/app.cache.js"), "Accept-Encoding", "br");
                assertEquals(200, brotli.statusCode());
                assertEquals(
                        "br", brotli.headers().firstValue("Content-Encoding").orElseThrow());
                assertEquals("encoded-js", brotli.body());

                final var jpaEndpoint = get(server.uri("/page-views"));
                assertEquals(404, jpaEndpoint.statusCode());
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    @Test
    public void jpaDeployJarPersistsPageViews() throws IOException, InterruptedException {
        final var tempDir = Files.createTempDirectory("ariake-jpa-example-test");
        try {
            final var config = tempDir.resolve("application.properties");
            final var databasePath = tempDir.resolve("pageviews");
            Files.writeString(
                    config,
                    "ariake.server.port=0\n"
                            + "ariake.server.shutdownHook=false\n"
                            + "jpa.example.databasePath="
                            + databasePath
                            + "\n");

            try (var server = ServerProcess.start(runfile("examples/jpa/server_deploy.jar"), config)) {
                assertEquals("{\"count\":0}", get(server.uri("/page-views")).body());

                final var created = post(server.uri("/page-views"), "/static/index.nocache.html");
                assertEquals(201, created.statusCode());
                assertEquals("{\"count\":1}", created.body());

                assertEquals("{\"count\":1}", get(server.uri("/page-views")).body());
            }
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private static HttpResponse<String> get(final URI uri) throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(uri).GET().build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static HttpResponse<String> get(final URI uri, final String headerName, final String headerValue)
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(uri)
                .header(headerName, headerValue)
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static HttpResponse<String> post(final URI uri, final String body)
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static Path runfile(final String path) throws IOException {
        final var testSrcDir = System.getenv("TEST_SRCDIR");
        final var testWorkspace = System.getenv("TEST_WORKSPACE");
        final var candidates = new ArrayList<Path>();
        if (null != testSrcDir && null != testWorkspace) {
            candidates.add(Path.of(testSrcDir, testWorkspace, path));
        }
        if (null != testSrcDir) {
            candidates.add(Path.of(testSrcDir, "_main", path));
        }
        candidates.add(Path.of(path).toAbsolutePath());

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        if (null != testSrcDir) {
            try (var paths = Files.walk(Path.of(testSrcDir), 8)) {
                final var suffix = Path.of(path);
                return paths.filter(Files::isRegularFile)
                        .filter(candidate -> candidate.endsWith(suffix))
                        .findFirst()
                        .orElseThrow(() -> new IOException("Unable to locate runfile: " + path));
            }
        }
        throw new IOException("Unable to locate runfile without TEST_SRCDIR: " + path);
    }

    private static void deleteDirectory(final Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var paths = Files.walk(path)) {
            final List<Path> entries = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path entry : entries) {
                Files.deleteIfExists(entry);
            }
        }
    }

    private static final class ServerProcess implements AutoCloseable {
        private final Process process;
        private final int port;
        private final Thread outputThread;
        private final List<String> outputLines;

        private ServerProcess(
                final Process process, final int port, final Thread outputThread, final List<String> outputLines) {
            this.process = process;
            this.port = port;
            this.outputThread = outputThread;
            this.outputLines = outputLines;
        }

        static ServerProcess start(final Path jar, final Path config) throws IOException, InterruptedException {
            final var outputLines = new ArrayList<String>();
            final var outputQueue = new LinkedBlockingQueue<String>();
            final var command = List.of(javaBinary().toString(), "-jar", jar.toString(), "--config=" + config);
            final var process =
                    new ProcessBuilder(command).redirectErrorStream(true).start();
            final var outputThread = new Thread(() -> readOutput(process, outputLines, outputQueue));
            outputThread.setDaemon(true);
            outputThread.start();

            final long deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                final var line = outputQueue.poll(100, TimeUnit.MILLISECONDS);
                if (null != line) {
                    final int port = parsePort(line);
                    if (0 != port) {
                        return new ServerProcess(process, port, outputThread, outputLines);
                    }
                }
                if (!process.isAlive()) {
                    throw new IllegalStateException("Server exited before startup: " + String.join("\n", outputLines));
                }
            }

            process.destroyForcibly();
            throw new IllegalStateException("Timed out waiting for server startup: " + String.join("\n", outputLines));
        }

        URI uri(final String path) {
            return URI.create("http://127.0.0.1:" + port + path);
        }

        @Override
        public void close() {
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    assertTrue("Server did not exit after destroyForcibly", process.waitFor(5, TimeUnit.SECONDS));
                }
                outputThread.join(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                throw new AssertionError("Interrupted while stopping server process", e);
            }
        }

        private static void readOutput(
                final Process process, final List<String> outputLines, final LinkedBlockingQueue<String> outputQueue) {
            try (var reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = reader.readLine())) {
                    outputLines.add(line);
                    outputQueue.offer(line);
                }
            } catch (IOException ignored) {
                outputLines.add("Unable to read server output");
            }
        }

        private static int parsePort(final String line) {
            final String marker = "started on port ";
            final int index = line.indexOf(marker);
            if (-1 == index) {
                return 0;
            }
            return Integer.parseInt(line.substring(index + marker.length()).trim());
        }

        private static Path javaBinary() {
            return Path.of(System.getProperty("java.home"), "bin", "java");
        }
    }
}
