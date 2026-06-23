package org.ariake.metrics;

import org.ariake.http.AriakeHttpService;
import org.ariake.http.HttpRoutes;

public final class MetricsHttpService implements AriakeHttpService {
    private final Metrics metrics;

    public MetricsHttpService(final Metrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public void routes(final HttpRoutes routes) {
        routes.get("/metrics", exchange -> {
            exchange.header("Content-Type", metrics.contentType());
            exchange.send(metrics.scrape());
        });
    }
}
