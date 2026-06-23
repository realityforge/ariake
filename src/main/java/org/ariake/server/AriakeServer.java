package org.ariake.server;

public interface AriakeServer extends AutoCloseable {
    AriakeServer start();

    AriakeServer stop();

    int port();

    boolean isRunning();

    @Override
    default void close() {
        stop();
    }
}
