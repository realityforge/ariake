package org.ariake.examples.jpa;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;

public final class Main {
    private Main() {}

    public static void main(final String[] args) throws InterruptedException {
        final Path configPath = parseConfigPath(args);
        final AriakeServer server = JpaApplication.create(configPath).server().start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        System.out.println("Ariake JPA server started on port " + server.port());
        System.out.println("Visit http://127.0.0.1:" + server.port() + "/page-views and expect a JSON count.");
        System.out.println(
                "POST a path to http://127.0.0.1:" + server.port() + "/page-views to persist and increment the count.");
        Thread.currentThread().join();
    }

    private static Path parseConfigPath(final String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--config=")) {
                return Path.of(arg.substring("--config=".length()));
            }
        }
        return Path.of("examples/jpa/application.properties");
    }
}
