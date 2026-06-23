package org.ariake.http;

@FunctionalInterface
public interface HttpHandler {
    void handle(HttpExchange exchange) throws Exception;
}
