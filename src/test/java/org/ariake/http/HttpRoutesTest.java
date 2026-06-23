package org.ariake.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class HttpRoutesTest {
    @Test
    public void registersEndpointsInOrder() {
        final HttpRoutes routes = new HttpRoutes();

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
}
