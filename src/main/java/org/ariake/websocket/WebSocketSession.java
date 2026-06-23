package org.ariake.websocket;

public interface WebSocketSession {
    void sendText(String text);

    void close(int status, String reason);
}
