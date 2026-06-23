package org.ariake.http;

@FunctionalInterface
public interface HttpFilterChain {
    void proceed(HttpExchange exchange) throws Exception;
}
