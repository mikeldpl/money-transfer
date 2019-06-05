package com.github.mikeldpl.hw.money.transfer.service;

public interface TransactionExecutorService {

    /**
     * Execute lambda in existing or new transaction
     */
    <T> T executeTransaction(boolean readOnly, TransactionalExecution<T> transactionalExecution);

    @FunctionalInterface
    interface TransactionalExecution<T> {
        T execute();
    }
}
