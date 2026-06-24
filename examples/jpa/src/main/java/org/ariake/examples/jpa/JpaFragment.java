package org.ariake.examples.jpa;

import java.io.IOException;
import java.nio.file.Files;
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
interface JpaFragment {
    default AriakeConfig config(final Path configPath) {
        try {
            return AriakeConfig.load(configPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load Ariake configuration: " + configPath, e);
        }
    }

    default EntityManagerProvider entityManagerProvider(final AriakeConfig config) {
        final Path databasePath = Path.of(config.get("jpa.example.databasePath", "tmp/jpa-example/pageviews"))
                .toAbsolutePath()
                .normalize();
        try {
            final Path parent = databasePath.getParent();
            if (null != parent) {
                Files.createDirectories(parent);
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
        return EclipseLinkEntityManagerProvider.create("jpa-example", Map.copyOf(properties));
    }

    default AriakeServer server(final AriakeConfig config, final PageViewService pageViewService) {
        return HelidonAriakeServer.create(
                config, List.<AriakeHttpService>of(pageViewService), List.<AriakeWebSocketService>of());
    }
}
