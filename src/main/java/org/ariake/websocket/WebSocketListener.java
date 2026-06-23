package org.ariake.websocket;

public interface WebSocketListener {
    default void onOpen(final WebSocketSession session) {}

    default void onText(final WebSocketSession session, final String text, final boolean last) {}

    default void onClose(final WebSocketSession session, final int status, final String reason) {}

    default void onError(final WebSocketSession session, final Throwable error) {}
}
