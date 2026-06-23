package org.ariake.metrics;

public interface Counter {
    void increment();

    void add(double amount);
}
