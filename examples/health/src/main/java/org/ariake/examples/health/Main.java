package org.ariake.examples.health;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;

public final class Main {
    private Main() {}

    public static void main(final String[] args) throws InterruptedException {
        final Path configPath = parseConfigPath(args);
        final AriakeServer server =
                HealthApplication.create(configPath).server().start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Ariake health server started on port " + server.port());
        System.out.println("Visit http://127.0.0.1:" + server.port() + "/health and expect an OK response.");
        System.out.println("Visit http://127.0.0.1:" + server.port() + "/metrics to see Prometheus metrics.");
        Thread.currentThread().join();
    }

    private static Path parseConfigPath(final String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                return Path.of(arg.substring("--config=".length()));
            }
        }
        return Path.of("examples/health/application.properties");
    }
}
