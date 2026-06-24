package org.ariake.examples.health;

import org.ariake.metrics.Counter;
import org.ariake.metrics.Metrics;
import sting.Injectable;
import sting.Typed;

@Injectable
@Typed(HealthResponder.class)
public final class HealthResponderImpl implements HealthResponder {
    private final Counter requests;

    HealthResponderImpl(final Metrics metrics) {
        requests = metrics.counter("ariake_health_requests", "Health endpoint requests");
    }

    @Override
    public String health() {
        requests.increment();
        return "OK";
    }
}
