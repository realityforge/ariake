package org.ariake.server.helidon;

import io.helidon.http.Method;
import io.helidon.http.Status;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.websocket.WsRouting;
import java.util.List;
import org.ariake.config.AriakeConfig;
import org.ariake.http.AriakeHttpService;
import org.ariake.http.HttpEndpoint;
import org.ariake.http.HttpExchange;
import org.ariake.http.HttpMethod;
import org.ariake.http.HttpRoutes;
import org.ariake.server.AriakeServer;
import org.ariake.websocket.AriakeWebSocketService;
import org.ariake.websocket.WebSocketEndpoint;

public final class HelidonAriakeServer implements AriakeServer {
    private final WebServer webServer;

    private HelidonAriakeServer(final WebServer webServer) {
        this.webServer = webServer;
    }

    public static HelidonAriakeServer create(
            final AriakeConfig config,
            final List<? extends AriakeHttpService> httpServices,
            final List<? extends AriakeWebSocketService> webSocketServices) {
        final int port = config.getInt("ariake.server.port", 8080);
        final WebServer server = WebServer.builder()
                .port(port)
                .routing(routing -> registerHttpServices(routing, httpServices))
                .addRouting(webSocketRouting(webSocketServices))
                .shutdownHook(config.getBoolean("ariake.server.shutdownHook", true))
                .build();
        return new HelidonAriakeServer(server);
    }

    private static void registerHttpServices(
            final HttpRouting.Builder routing, final List<? extends AriakeHttpService> services) {
        for (AriakeHttpService service : services) {
            final HttpRoutes routes = new HttpRoutes();
            service.routes(routes);
            for (HttpEndpoint endpoint : routes.endpoints()) {
                registerHttpEndpoint(routing, endpoint);
            }
        }
    }

    private static WsRouting.Builder webSocketRouting(final List<? extends AriakeWebSocketService> services) {
        final WsRouting.Builder routing = WsRouting.builder();
        for (AriakeWebSocketService service : services) {
            for (WebSocketEndpoint endpoint : service.endpoints()) {
                routing.endpoint(endpoint.path(), new HelidonWebSocketListener(endpoint.listener()));
            }
        }
        return routing;
    }

    private static void registerHttpEndpoint(final HttpRouting.Builder routing, final HttpEndpoint endpoint) {
        switch (endpoint.method()) {
            case GET -> routing.get(endpoint.path(), (request, response) -> handle(endpoint, request, response));
            case POST -> routing.post(endpoint.path(), (request, response) -> handle(endpoint, request, response));
            case PUT -> routing.put(endpoint.path(), (request, response) -> handle(endpoint, request, response));
            case DELETE -> routing.delete(endpoint.path(), (request, response) -> handle(endpoint, request, response));
            case PATCH -> routing.patch(endpoint.path(), (request, response) -> handle(endpoint, request, response));
            case OPTIONS ->
                routing.options(endpoint.path(), (request, response) -> handle(endpoint, request, response));
            case HEAD -> routing.head(endpoint.path(), (request, response) -> handle(endpoint, request, response));
        }
    }

    private static Method helidonMethod(final HttpMethod method) {
        return switch (method) {
            case GET -> Method.GET;
            case POST -> Method.POST;
            case PUT -> Method.PUT;
            case DELETE -> Method.DELETE;
            case PATCH -> Method.PATCH;
            case OPTIONS -> Method.OPTIONS;
            case HEAD -> Method.HEAD;
        };
    }

    private static void handle(
            final HttpEndpoint endpoint, final ServerRequest request, final ServerResponse response) {
        if (!request.prologue().method().equals(helidonMethod(endpoint.method()))) {
            response.status(Status.METHOD_NOT_ALLOWED_405).send();
            return;
        }
        try {
            endpoint.handler().handle(new HelidonHttpExchange(request, response));
        } catch (Exception e) {
            if (!response.isSent()) {
                response.status(Status.INTERNAL_SERVER_ERROR_500).send("Internal Server Error");
            }
        }
    }

    @Override
    public HelidonAriakeServer start() {
        webServer.start();
        return this;
    }

    @Override
    public HelidonAriakeServer stop() {
        webServer.stop();
        return this;
    }

    @Override
    public int port() {
        return webServer.port();
    }

    @Override
    public boolean isRunning() {
        return webServer.isRunning();
    }

    private record HelidonHttpExchange(ServerRequest request, ServerResponse response) implements HttpExchange {
        @Override
        public String path() {
            return request.path().path();
        }

        @Override
        public String body() {
            return request.content().as(String.class);
        }

        @Override
        public void status(final int status) {
            response.status(status);
        }

        @Override
        public void header(final String name, final String value) {
            response.header(name, value);
        }

        @Override
        public void send() {
            response.send();
        }

        @Override
        public void send(final String body) {
            response.send(body);
        }

        @Override
        public void send(final byte[] body, final String contentType) {
            response.header("Content-Type", contentType);
            response.send(body);
        }
    }
}
