package org.ariake.tx.narayana;

import jakarta.transaction.TransactionManager;

public final class NarayanaTransactions {
    private NarayanaTransactions() {}

    public static TransactionManager transactionManager() {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }
}
