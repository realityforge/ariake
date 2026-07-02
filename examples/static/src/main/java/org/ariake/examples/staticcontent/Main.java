package org.ariake.examples.staticcontent;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;

public final class Main {
    private Main() {}

    public static void main(final String[] args) throws InterruptedException {
        final Path configPath = parseConfigPath(args);
        final AriakeServer server =
                StaticContentApplication.create(configPath).server().start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Ariake static content server started on port " + server.port());
        System.out.println("Visit http://127.0.0.1:" + server.port() + "/static and expect the static HTML page.");
        System.out.println("Fetch http://127.0.0.1:" + server.port()
                + "/static/app.cache.js with Accept-Encoding: br to receive the Brotli sidecar.");
        Thread.currentThread().join();
    }

    private static Path parseConfigPath(final String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                return Path.of(arg.substring("--config=".length()));
            }
        }
        return Path.of("examples/static/application.properties");
    }
}
