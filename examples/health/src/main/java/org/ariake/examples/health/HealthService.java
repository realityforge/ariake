package org.ariake.examples.health;

import io.helidon.webserver.http.HttpRouting;
import org.ariake.server.HttpRoutingService;
import sting.Injectable;

@Injectable
public final class HealthService implements HttpRoutingService {
    private final HealthResponder responder;

    HealthService(final HealthResponder responder) {
        this.responder = responder;
    }

    @Override
    public void routing(final HttpRouting.Builder routing) {
        routing.get("/health", (request, response) -> response.send(responder.health()));
    }
}
