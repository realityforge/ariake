package org.ariake.websocket;

import java.util.ArrayList;
import java.util.List;

public final class WebSocketRoutes {
    private final ArrayList<WebSocketEndpoint> endpoints = new ArrayList<>();

    public void endpoint(final String path, final WebSocketListener listener) {
        endpoints.add(new WebSocketEndpoint(path, listener));
    }

    public List<WebSocketEndpoint> endpoints() {
        return List.copyOf(endpoints);
    }
}
