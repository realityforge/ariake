package org.ariake.examples.websocket;

import io.helidon.webserver.websocket.WsRouting;
import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;
import org.ariake.server.WebSocketRoutingService;
import sting.Injectable;

@Injectable
public final class EchoWebSocketService implements WebSocketRoutingService, WsListener {
    @Override
    public void routing(final WsRouting.Builder routing) {
        routing.endpoint("/ws/echo", this);
    }

    @Override
    public void onMessage(final WsSession session, final String text, final boolean last) {
        if (last) {
            session.send(text, true);
        }
    }
}
