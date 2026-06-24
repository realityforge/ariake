package org.ariake.examples.websocket;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.ariake.config.AriakeConfig;
import org.ariake.server.AriakeServer;
import org.ariake.server.HttpRoutingService;
import org.ariake.server.WebSocketRoutingService;
import sting.Fragment;

@Fragment
public interface WebSocketFragment {
    default AriakeConfig config(final Path configPath) {
        try {
            return AriakeConfig.load(configPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load Ariake configuration: " + configPath, e);
        }
    }

    default AriakeServer server(
            final AriakeConfig config,
            final WebSocketPageService pageService,
            final EchoWebSocketService echoWebSocketService) {
        return AriakeServer.create(
                config,
                List.<HttpRoutingService>of(pageService),
                List.<WebSocketRoutingService>of(echoWebSocketService));
    }
}
