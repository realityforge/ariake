package org.ariake.examples.hello;

import org.ariake.http.AriakeHttpService;
import org.ariake.http.HttpRoutes;
import sting.Injectable;

@Injectable
public final class HealthService implements AriakeHttpService {
    private final HealthResponder responder;

    HealthService(final HealthResponder responder) {
        this.responder = responder;
    }

    @Override
    public void routes(final HttpRoutes routes) {
        routes.get("/health", exchange -> exchange.send(responder.health()));
    }
}
