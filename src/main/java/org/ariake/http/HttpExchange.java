package org.ariake.http;

import java.time.Instant;
import java.util.Optional;

public interface HttpExchange {
    HttpMethod method();

    String path();

    String requestUri();

    Optional<String> requestHeader(String name);

    String body();

    void status(int status);

    void header(String name, String value);

    void dateHeader(String name, Instant value);

    void send();

    void send(String body);

    void send(byte[] body, String contentType);

    void sendError(int status, String message);

    boolean isSent();
}
