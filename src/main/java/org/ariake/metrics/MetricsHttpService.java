package org.ariake.metrics;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import org.ariake.server.HttpRoutingService;

public final class MetricsHttpService implements HttpRoutingService {
    private final Metrics metrics;

    public MetricsHttpService(final Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void routing(final HttpRouting.Builder routing) {
        routing.get("/metrics", this::scrape);
    }

    private void scrape(final ServerRequest request, final ServerResponse response) {
        response.header("Content-Type", metrics.contentType());
        response.send(metrics.scrape());
    }
}
