package org.ariake.examples.health;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.ariake.config.AriakeConfig;
import org.ariake.http.AriakeHttpService;
import org.ariake.metrics.Metrics;
import org.ariake.metrics.MetricsHttpService;
import org.ariake.metrics.prometheus.PrometheusMetrics;
import org.ariake.server.AriakeServer;
import org.ariake.server.helidon.HelidonAriakeServer;
import org.ariake.websocket.AriakeWebSocketService;
import sting.Fragment;

@Fragment
public interface HealthFragment {
    default AriakeConfig config(final Path configPath) {
        try {
            return AriakeConfig.load(configPath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load Ariake configuration: " + configPath, e);
        }
    }

    default Metrics metrics() {
        return PrometheusMetrics.create();
    }

    default MetricsHttpService metricsHttpService(final Metrics metrics) {
        return new MetricsHttpService(metrics);
    }

    default AriakeServer server(
            final AriakeConfig config, final HealthService healthService, final MetricsHttpService metricsHttpService) {
        return HelidonAriakeServer.create(
                config,
                List.<AriakeHttpService>of(healthService, metricsHttpService),
                List.<AriakeWebSocketService>of());
    }
}
