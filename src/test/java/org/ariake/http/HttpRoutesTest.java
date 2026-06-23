package org.ariake.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public final class HttpRoutesTest {
    @Test
    public void registersEndpointsInOrder() {
        final var routes = new HttpRoutes();

        routes.get("/health", exchange -> exchange.send("OK"));
        routes.post("/items", exchange -> exchange.status(201));

        assertEquals(2, routes.endpoints().size());
        assertEquals(
                new HttpEndpoint(
                        HttpMethod.GET, "/health", routes.endpoints().get(0).handler()),
                routes.endpoints().get(0));
        assertEquals(HttpMethod.POST, routes.endpoints().get(1).method());
        assertEquals("/items", routes.endpoints().get(1).path());
    }

    @Test
    public void returnsMatchingFiltersInOrder() {
        final var routes = new HttpRoutes();

        routes.filter(20, "/api/*", (exchange, chain) -> chain.proceed(exchange));
        routes.filter(10, "/api/items", (exchange, chain) -> chain.proceed(exchange));
        routes.filter(30, "/assets/*", (exchange, chain) -> chain.proceed(exchange));

        final List<HttpFilterRegistration> filters = routes.matchingFilters("/api/items");
        assertEquals(2, filters.size());
        assertEquals(10, filters.get(0).order());
        assertEquals(20, filters.get(1).order());
        assertTrue(routes.matchingFilters("/api/items/1").stream().allMatch(filter -> filter.order() == 20));
        assertTrue(routes.matchingFilters("/api-items").isEmpty());
    }

    @Test
    public void rejectsUnsupportedFilterPatterns() {
        final var routes = new HttpRoutes();

        assertThrows(
                IllegalArgumentException.class,
                () -> routes.filter(0, "api/*", (exchange, chain) -> chain.proceed(exchange)));
        assertThrows(
                IllegalArgumentException.class,
                () -> routes.filter(0, "/api/*.js", (exchange, chain) -> chain.proceed(exchange)));
    }

    @Test
    public void dispatchesThroughMatchingFiltersAroundHandler() throws Exception {
        final var routes = new HttpRoutes();
        final ArrayList<String> events = new ArrayList<>();
        final var exchange = new RecordingHttpExchange(HttpMethod.GET, "/api/items");

        routes.filter(20, "/api/*", (httpExchange, chain) -> {
            events.add("filter-20-before");
            chain.proceed(httpExchange);
            events.add("filter-20-after");
        });
        routes.filter(10, "/*", (httpExchange, chain) -> {
            events.add("filter-10-before");
            chain.proceed(httpExchange);
            events.add("filter-10-after");
        });
        routes.filter(30, "/assets/*", (httpExchange, chain) -> events.add("unmatched"));

        routes.handle(exchange, httpExchange -> {
            events.add("handler");
            httpExchange.send("OK");
        });

        assertEquals(
                List.of("filter-10-before", "filter-20-before", "handler", "filter-20-after", "filter-10-after"),
                events);
        assertEquals("OK", exchange.sentBody());
    }

    @Test
    public void shortCircuitsWhenFilterSendsResponse() throws Exception {
        final var routes = new HttpRoutes();
        final ArrayList<String> events = new ArrayList<>();
        final var exchange = new RecordingHttpExchange(HttpMethod.GET, "/api/session");

        routes.filter(0, "/api/*", (httpExchange, chain) -> {
            events.add("guard");
            httpExchange.sendError(403, "Forbidden");
        });
        routes.filter(10, "/api/*", (httpExchange, chain) -> events.add("not-called"));

        routes.handle(exchange, httpExchange -> events.add("handler"));

        assertEquals(List.of("guard"), events);
        assertEquals(403, exchange.status());
        assertEquals("Forbidden", exchange.sentBody());
        assertTrue(exchange.isSent());
    }

    @Test
    public void exchangeHelpersExposeRequestAndResponseState() throws Exception {
        final var routes = new HttpRoutes();
        final var exchange = new RecordingHttpExchange(HttpMethod.POST, "/rose/react.js");
        exchange.requestHeader("Accept-Encoding", "gzip, br");

        routes.filter(0, "/rose/*", (httpExchange, chain) -> {
            assertEquals(HttpMethod.POST, httpExchange.method());
            assertEquals("/rose/react.js", httpExchange.path());
            assertEquals("/rose/react.js?debug=true", httpExchange.requestUri());
            assertEquals(Optional.of("gzip, br"), httpExchange.requestHeader("Accept-Encoding"));
            assertFalse(httpExchange.isSent());
            httpExchange.dateHeader("Expires", Instant.EPOCH);
            httpExchange.header("Cache-control", "no-cache");
            chain.proceed(httpExchange);
        });

        routes.handle(exchange, httpExchange -> httpExchange.send("OK"));

        assertEquals("Thu, 1 Jan 1970 00:00:00 GMT", exchange.responseHeader("Expires"));
        assertEquals("no-cache", exchange.responseHeader("Cache-control"));
        assertEquals("OK", exchange.sentBody());
    }

    private static final class RecordingHttpExchange implements HttpExchange {
        private final HttpMethod method;
        private final String path;
        private final HashMap<String, String> requestHeaders = new HashMap<>();
        private final HashMap<String, String> responseHeaders = new HashMap<>();
        private int status = 200;
        private String sentBody = "";
        private boolean sent;

        private RecordingHttpExchange(final HttpMethod method, final String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public HttpMethod method() {
            return method;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public String requestUri() {
            return path + "?debug=true";
        }

        @Override
        public Optional<String> requestHeader(final String name) {
            return Optional.ofNullable(requestHeaders.get(name));
        }

        private void requestHeader(final String name, final String value) {
            requestHeaders.put(name, value);
        }

        @Override
        public String body() {
            return "";
        }

        @Override
        public void status(final int status) {
            this.status = status;
        }

        private int status() {
            return status;
        }

        @Override
        public void header(final String name, final String value) {
            responseHeaders.put(name, value);
        }

        @Override
        public void dateHeader(final String name, final Instant value) {
            responseHeaders.put(name, DateTimeFormatter.RFC_1123_DATE_TIME.format(value.atZone(ZoneOffset.UTC)));
        }

        private String responseHeader(final String name) {
            final String value = responseHeaders.get(name);
            if (null == value) {
                throw new IllegalArgumentException("Missing response header: " + name);
            }
            return value;
        }

        @Override
        public void send() {
            sent = true;
        }

        @Override
        public void send(final String body) {
            sentBody = body;
            sent = true;
        }

        @Override
        public void send(final byte[] body, final String contentType) {
            header("Content-Type", contentType);
            sentBody = new String(body, java.nio.charset.StandardCharsets.UTF_8);
            sent = true;
        }

        @Override
        public void sendError(final int status, final String message) {
            status(status);
            send(message);
        }

        private String sentBody() {
            return sentBody;
        }

        @Override
        public boolean isSent() {
            return sent;
        }
    }
}
