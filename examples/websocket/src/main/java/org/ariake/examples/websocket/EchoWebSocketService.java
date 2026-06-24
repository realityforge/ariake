package org.ariake.examples.websocket;

import java.util.List;
import org.ariake.websocket.AriakeWebSocketService;
import org.ariake.websocket.WebSocketEndpoint;
import org.ariake.websocket.WebSocketListener;
import org.ariake.websocket.WebSocketSession;
import sting.Injectable;

@Injectable
public final class EchoWebSocketService implements AriakeWebSocketService, WebSocketListener {
    @Override
    public List<WebSocketEndpoint> endpoints() {
        return List.of(new WebSocketEndpoint("/ws/echo", this));
    }

    @Override
    public void onText(final WebSocketSession session, final String text, final boolean last) {
        if (last) {
            session.sendText(text);
        }
    }
}
