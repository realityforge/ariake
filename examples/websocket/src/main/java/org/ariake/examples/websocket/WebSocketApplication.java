package org.ariake.examples.websocket;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;
import sting.Injector;

@Injector(fragmentOnly = false, inputs = @Injector.Input(type = Path.class), includes = WebSocketFragment.class)
public interface WebSocketApplication {
    static WebSocketApplication create(final Path configPath) {
        return new Sting_WebSocketApplication(configPath);
    }

    AriakeServer server();
}
