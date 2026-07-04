package org.ariake.metrics.prometheus;

import static org.junit.Assert.assertSame;
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

    @Test
    public void reusesCountersAndSupportsAddingAmounts() {
        final var metrics = PrometheusMetrics.create();
        final var counter = metrics.counter("ariake_test_amounts", "Test amounts");

        assertSame(counter, metrics.counter("ariake_test_amounts", "Ignored help"));
        counter.add(2.5);

        final String scrape = metrics.scrape();
        assertTrue(scrape, scrape.contains("ariake_test_amounts"));
        assertTrue(scrape, scrape.contains("2.5"));
        assertTrue(metrics.contentType(), metrics.contentType().contains("text/plain"));
    }
}
