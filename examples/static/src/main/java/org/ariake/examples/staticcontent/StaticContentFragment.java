package org.ariake.examples.staticcontent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.ariake.config.AriakeConfig;
import org.ariake.server.AriakeServer;
import org.ariake.server.helidon.HelidonAriakeServer;
import org.ariake.server.helidon.HelidonRoutingService;
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

    default AriakeServer server(final AriakeConfig config, final StaticContentRoutes staticContentRoutes) {
        return HelidonAriakeServer.create(
                config,
                List.of(),
                List.<HelidonRoutingService>of(staticContentRoutes),
                List.<AriakeWebSocketService>of());
    }
}
