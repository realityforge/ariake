package org.ariake.server;

import io.helidon.webserver.websocket.WsRouting;

@FunctionalInterface
public interface WebSocketRoutingService {
    void routing(WsRouting.Builder routing);
}
