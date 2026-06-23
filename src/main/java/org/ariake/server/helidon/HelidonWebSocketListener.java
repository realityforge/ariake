package org.ariake.server.helidon;

import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;
import org.ariake.websocket.WebSocketListener;
import org.ariake.websocket.WebSocketSession;

final class HelidonWebSocketListener implements WsListener {
    private final WebSocketListener listener;

    HelidonWebSocketListener(final WebSocketListener listener) {
        this.listener = listener;
    }

    @Override
    public void onOpen(final WsSession session) {
        listener.onOpen(new HelidonWebSocketSession(session));
    }

    @Override
    public void onMessage(final WsSession session, final String text, final boolean last) {
        listener.onText(new HelidonWebSocketSession(session), text, last);
    }

    @Override
    public void onClose(final WsSession session, final int status, final String reason) {
        listener.onClose(new HelidonWebSocketSession(session), status, reason);
    }

    @Override
    public void onError(final WsSession session, final Throwable error) {
        listener.onError(new HelidonWebSocketSession(session), error);
    }

    private record HelidonWebSocketSession(WsSession session) implements WebSocketSession {
        @Override
        public void sendText(final String text) {
            session.send(text, true);
        }

        @Override
        public void close(final int status, final String reason) {
            session.close(status, reason);
        }
    }
}
