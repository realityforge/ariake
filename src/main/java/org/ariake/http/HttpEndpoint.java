package org.ariake.http;

import java.util.Objects;

public record HttpEndpoint(HttpMethod method, String path, HttpHandler handler) {
    public HttpEndpoint {
        Objects.requireNonNull(method);
        Objects.requireNonNull(path);
        Objects.requireNonNull(handler);
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("HTTP route path must start with '/': " + path);
        }
    }
}
