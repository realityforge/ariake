package org.ariake.metrics;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.ariake.config.AriakeConfig;
import org.ariake.server.AriakeServer;
import org.ariake.server.HttpRoutingService;
import org.junit.Test;

public final class MetricsHttpServiceTest {
    @Test
    public void servesScrapedMetrics() throws IOException, InterruptedException {
        final var server = AriakeServer.create(
                        AriakeConfig.of(Map.of(
                                "ariake.server.port", "0",
                                "ariake.server.shutdownHook", "false")),
                        List.<HttpRoutingService>of(new MetricsHttpService(new TestMetrics())))
                .start();

        try {
            final var response = get(server.port(), "/metrics");

            assertEquals(200, response.statusCode());
            assertEquals(
                    "text/plain", response.headers().firstValue("Content-Type").orElseThrow());
            assertEquals("ariake_test_metric 1\n", response.body());
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

    private static final class TestMetrics implements Metrics {
        @Override
        public Counter counter(final String name, final String help) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String scrape() {
            return "ariake_test_metric 1\n";
        }

        @Override
        public String contentType() {
            return "text/plain";
        }
    }
}
