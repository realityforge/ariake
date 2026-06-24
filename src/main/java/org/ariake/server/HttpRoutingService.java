package org.ariake.server;

import io.helidon.webserver.http.HttpRouting;

@FunctionalInterface
public interface HttpRoutingService {
    void routing(HttpRouting.Builder routing);
}
