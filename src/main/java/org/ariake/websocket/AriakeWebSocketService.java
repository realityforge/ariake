package org.ariake.websocket;

import java.util.List;

@FunctionalInterface
public interface AriakeWebSocketService {
    List<WebSocketEndpoint> endpoints();
}
