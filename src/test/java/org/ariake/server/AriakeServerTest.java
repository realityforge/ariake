package org.ariake.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import io.helidon.websocket.WsListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ariake.config.AriakeConfig;
import org.junit.Test;

public final class AriakeServerTest {
    @Test
    public void servesHttpRoutesAndReportsLifecycle() throws IOException, InterruptedException {
        final var config = AriakeConfig.of(Map.of(
                "ariake.server.port", "0",
                "ariake.server.shutdownHook", "false"));
        final var server = AriakeServer.create(
                config,
                List.<HttpRoutingService>of(routing -> routing.get("/hello", (request, response) -> {
                    response.send("hello");
                })));

        try {
            assertFalse(server.isRunning());
            assertSame(server, server.start());
            assertTrue(server.isRunning());

            final var response = get(server.port(), "/hello");
            assertEquals(200, response.statusCode());
            assertEquals("hello", response.body());

            assertSame(server, server.stop());
            assertFalse(server.isRunning());
        } finally {
            server.close();
        }
    }

    @Test
    public void registersWebSocketRoutes() {
        final var registered = new AtomicBoolean();
        final var server = AriakeServer.create(
                AriakeConfig.of(Map.of("ariake.server.port", "0")),
                List.<HttpRoutingService>of(),
                List.<WebSocketRoutingService>of(routing -> {
                    registered.set(true);
                    routing.endpoint("/ws", new WsListener() {});
                }));

        try {
            assertTrue(registered.get());
        } finally {
            server.close();
        }
    }

    private static HttpResponse<String> get(final int port, final String path)
            throws IOException, InterruptedException {
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
