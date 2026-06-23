package org.ariake.examples.staticcontent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ariake.config.AriakeConfig;
import org.ariake.http.AriakeHttpService;
import org.ariake.jpa.EntityManagerProvider;
import org.ariake.jpa.eclipselink.EclipseLinkEntityManagerProvider;
import org.ariake.server.AriakeServer;
import org.ariake.server.helidon.HelidonAriakeServer;
import org.ariake.websocket.AriakeWebSocketService;
import sting.Fragment;

@Fragment
public interface StaticContentFragment {
    default AriakeConfig config(final Path configPath) {
        try {
            return AriakeConfig.load(configPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load Ariake configuration: " + configPath, e);
        }
    }

    default StaticContentRoot staticContentRoot(final AriakeConfig config) {
        return StaticContentRoot.locate(config);
    }

    default EntityManagerProvider entityManagerProvider(final AriakeConfig config) {
        final Path databasePath = Path.of(
                        config.get("static.example.databasePath", "tmp/static-content-example/pageviews"))
                .toAbsolutePath()
                .normalize();
        try {
            final Path parent = databasePath.getParent();
            if (null != parent) {
                java.nio.file.Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create database directory for " + databasePath, e);
        }

        final HashMap<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:file:" + databasePath);
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("eclipselink.ddl-generation", "create-or-extend-tables");
        properties.put("eclipselink.logging.level", "WARNING");
        return EclipseLinkEntityManagerProvider.create("static-content-example", Map.copyOf(properties));
    }

    default AriakeServer server(
            final AriakeConfig config,
            final StaticContentService staticContentService,
            final PageViewService pageViewService) {
        return HelidonAriakeServer.create(
                config,
                List.<AriakeHttpService>of(staticContentService, pageViewService),
                List.<AriakeWebSocketService>of());
    }
}
