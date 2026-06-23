package org.ariake.metrics.prometheus;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.expositionformats.PrometheusTextFormatWriter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.ariake.metrics.Metrics;

public final class PrometheusMetrics implements Metrics {
    private final PrometheusRegistry registry;
    private final PrometheusTextFormatWriter writer;
    private final ConcurrentHashMap<String, org.ariake.metrics.Counter> counters = new ConcurrentHashMap<>();

    private PrometheusMetrics(final PrometheusRegistry registry) {
        this.registry = registry;
        writer = PrometheusTextFormatWriter.create();
    }

    public static PrometheusMetrics create() {
        return new PrometheusMetrics(new PrometheusRegistry());
    }

    @Override
    public org.ariake.metrics.Counter counter(final String name, final String help) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(help);
        return counters.computeIfAbsent(name, ignored -> {
            final Counter counter = Counter.builder().name(name).help(help).register(registry);
            return new PrometheusCounter(counter);
        });
    }

    @Override
    public String scrape() {
        final var output = new ByteArrayOutputStream();
        try {
            writer.write(output, registry.scrape());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to render Prometheus metrics", e);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @Override
    public String contentType() {
        return writer.getContentType();
    }

    private record PrometheusCounter(Counter counter) implements org.ariake.metrics.Counter {
        @Override
        public void increment() {
            counter.inc(1);
        }

        @Override
        public void add(final double amount) {
            counter.inc(amount);
        }
    }
}
