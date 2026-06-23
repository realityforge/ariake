package org.ariake.http;

public interface HttpExchange {
    String path();

    String body();

    void status(int status);

    void header(String name, String value);

    void send();

    void send(String body);

    void send(byte[] body, String contentType);
}
