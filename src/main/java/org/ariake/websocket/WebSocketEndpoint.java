package org.ariake.websocket;

import java.util.Objects;

public record WebSocketEndpoint(String path, WebSocketListener listener) {
    public WebSocketEndpoint {
        Objects.requireNonNull(path);
        Objects.requireNonNull(listener);
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("WebSocket path must start with '/': " + path);
        }
    }
}
