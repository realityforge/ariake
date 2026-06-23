package org.ariake.metrics;

public interface Metrics {
    Counter counter(String name, String help);

    String scrape();

    String contentType();
}
