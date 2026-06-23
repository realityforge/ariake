package org.ariake.metrics.prometheus;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PrometheusMetricsTest {
    @Test
    public void exposesRegisteredCounter() {
        final var metrics = PrometheusMetrics.create();

        metrics.counter("ariake_test_events", "Test events").increment();

        final String scrape = metrics.scrape();
        assertTrue(scrape, scrape.contains("ariake_test_events"));
        assertTrue(scrape, scrape.contains("Test events"));
    }
}
