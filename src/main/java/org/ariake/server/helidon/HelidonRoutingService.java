package org.ariake.server.helidon;

import io.helidon.webserver.http.HttpRouting;

@FunctionalInterface
public interface HelidonRoutingService {
    void routing(HttpRouting.Builder routing);
}
