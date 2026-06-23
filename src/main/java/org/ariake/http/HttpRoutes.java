package org.ariake.http;

import java.util.ArrayList;
import java.util.List;

public final class HttpRoutes {
    private final ArrayList<HttpEndpoint> endpoints = new ArrayList<>();

    public void get(final String path, final HttpHandler handler) {
        route(HttpMethod.GET, path, handler);
    }

    public void post(final String path, final HttpHandler handler) {
        route(HttpMethod.POST, path, handler);
    }

    public void put(final String path, final HttpHandler handler) {
        route(HttpMethod.PUT, path, handler);
    }

    public void delete(final String path, final HttpHandler handler) {
        route(HttpMethod.DELETE, path, handler);
    }

    public void patch(final String path, final HttpHandler handler) {
        route(HttpMethod.PATCH, path, handler);
    }

    public void route(final HttpMethod method, final String path, final HttpHandler handler) {
        endpoints.add(new HttpEndpoint(method, path, handler));
    }

    public List<HttpEndpoint> endpoints() {
        return List.copyOf(endpoints);
    }
}
