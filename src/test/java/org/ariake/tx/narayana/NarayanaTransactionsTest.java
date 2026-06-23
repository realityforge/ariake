package org.ariake.tx.narayana;

import static org.junit.Assert.assertEquals;

import jakarta.transaction.Status;
import jakarta.transaction.TransactionManager;
import org.junit.Test;

public final class NarayanaTransactionsTest {
    @Test
    public void returnsWorkingTransactionManager() throws Exception {
        final TransactionManager transactionManager = NarayanaTransactions.transactionManager();

        transactionManager.begin();
        try {
            assertEquals(Status.STATUS_ACTIVE, transactionManager.getStatus());
        } finally {
            if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                transactionManager.rollback();
            }
        }
    }
}
