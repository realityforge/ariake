package org.ariake.examples.hello;

import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import org.ariake.metrics.Counter;
import org.ariake.metrics.Metrics;
import sting.Injectable;
import sting.Typed;

@Injectable
@Typed(HealthResponder.class)
public final class HealthResponderImpl implements HealthResponder {
    private final Counter requests;
    private final TransactionManager transactionManager;

    HealthResponderImpl(final Metrics metrics, final TransactionManager transactionManager) {
        requests = metrics.counter("ariake_health_requests", "Health endpoint requests");
        this.transactionManager = transactionManager;
    }

    @Override
    public String health() {
        verifyActiveTransaction();
        requests.increment();
        return "OK";
    }

    private void verifyActiveTransaction() {
        try {
            if (transactionManager.getStatus() != Status.STATUS_ACTIVE) {
                throw new IllegalStateException("Expected active transaction for health response");
            }
        } catch (SystemException e) {
            throw new IllegalStateException("Unable to verify health response transaction", e);
        }
    }
}
