package org.ariake.http;

@FunctionalInterface
public interface HttpFilter {
    void filter(HttpExchange exchange, HttpFilterChain chain) throws Exception;
}
