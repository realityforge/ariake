package org.ariake.server;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.websocket.WsRouting;
import java.util.List;
import org.ariake.config.AriakeConfig;

public final class AriakeServer implements AutoCloseable {
    private final WebServer webServer;

    private AriakeServer(final WebServer webServer) {
        this.webServer = webServer;
    }

    public static AriakeServer create(
            final AriakeConfig config, final List<? extends HttpRoutingService> httpRoutingServices) {
        return create(config, httpRoutingServices, List.of());
    }

    public static AriakeServer create(
            final AriakeConfig config,
            final List<? extends HttpRoutingService> httpRoutingServices,
            final List<? extends WebSocketRoutingService> webSocketRoutingServices) {
        final int port = config.getInt("ariake.server.port", 8080);
        final var builder = WebServer.builder()
                .port(port)
                .routing(routing -> registerHttpRoutingServices(routing, httpRoutingServices))
                .shutdownHook(config.getBoolean("ariake.server.shutdownHook", true));
        if (!webSocketRoutingServices.isEmpty()) {
            builder.addRouting(webSocketRouting(webSocketRoutingServices));
        }
        return new AriakeServer(builder.build());
    }

    private static void registerHttpRoutingServices(
            final HttpRouting.Builder routing, final List<? extends HttpRoutingService> httpRoutingServices) {
        for (HttpRoutingService service : httpRoutingServices) {
            service.routing(routing);
        }
    }

    private static WsRouting.Builder webSocketRouting(
            final List<? extends WebSocketRoutingService> webSocketRoutingServices) {
        final WsRouting.Builder routing = WsRouting.builder();
        for (WebSocketRoutingService service : webSocketRoutingServices) {
            service.routing(routing);
        }
        return routing;
    }

    public AriakeServer start() {
        webServer.start();
        return this;
    }

    public AriakeServer stop() {
        webServer.stop();
        return this;
    }

    public int port() {
        return webServer.port();
    }

    public boolean isRunning() {
        return webServer.isRunning();
    }

    @Override
    public void close() {
        stop();
    }
}
