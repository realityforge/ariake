package org.ariake.http;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HttpRoutes {
    private final ArrayList<HttpEndpoint> endpoints = new ArrayList<>();
    private final ArrayList<HttpFilterRegistration> filters = new ArrayList<>();

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

    public void filter(final int order, final String pathPattern, final HttpFilter filter) {
        filter(order, List.of(pathPattern), filter);
    }

    public void filter(final int order, final List<String> pathPatterns, final HttpFilter filter) {
        filters.add(new HttpFilterRegistration(order, pathPatterns, filter));
    }

    public List<HttpEndpoint> endpoints() {
        return List.copyOf(endpoints);
    }

    public List<HttpFilterRegistration> filters() {
        final ArrayList<HttpFilterRegistration> orderedFilters = new ArrayList<>(filters);
        orderedFilters.sort(Comparator.comparingInt(HttpFilterRegistration::order));
        return List.copyOf(orderedFilters);
    }

    public List<HttpFilterRegistration> matchingFilters(final String path) {
        return filters().stream().filter(filter -> filter.matches(path)).toList();
    }

    public void handle(final HttpExchange exchange, final HttpHandler handler) throws Exception {
        new RoutingHttpFilterChain(matchingFilters(exchange.path()), handler).proceed(exchange);
    }

    private static final class RoutingHttpFilterChain implements HttpFilterChain {
        private final List<HttpFilterRegistration> filters;
        private final HttpHandler handler;
        private int index;

        private RoutingHttpFilterChain(final List<HttpFilterRegistration> filters, final HttpHandler handler) {
            this.filters = filters;
            this.handler = handler;
        }

        @Override
        public void proceed(final HttpExchange exchange) throws Exception {
            if (exchange.isSent()) {
                return;
            }
            if (index < filters.size()) {
                filters.get(index++).filter().filter(exchange, this);
            } else {
                handler.handle(exchange);
            }
        }
    }
}
