package com.github.mikeldpl.hw.money.transfer.service;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import com.github.mikeldpl.hw.money.transfer.exception.DbApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionExecutorJdbcService implements TransactionExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExecutorJdbcService.class);
    private static final ThreadLocal<Connection> CURRENT_CONNECTION_HOLDER = new ThreadLocal<>();

    private final DataSource dataSource;
    private final int transactionIsolation;

    public TransactionExecutorJdbcService(DataSource dataSource, int transactionIsolation) {
        this.dataSource = dataSource;
        this.transactionIsolation = transactionIsolation;
    }

    @Override
    public <T> T executeTransaction(boolean readOnly, TransactionalExecution<T> transactionalExecution) {
        T result;
        Connection connection = CURRENT_CONNECTION_HOLDER.get();
        if (connection != null) {
            result = transactionalExecution.execute();
        } else {
            result = executeNew(transactionalExecution, readOnly);
        }
        return result;
    }

    private <T> T executeNew(TransactionalExecution<T> transactionalExecution, boolean readOnly) {
        Connection oldConnection = CURRENT_CONNECTION_HOLDER.get();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.setReadOnly(readOnly);
            connection.setTransactionIsolation(transactionIsolation);
            CURRENT_CONNECTION_HOLDER.set(connection);
            T result;
            try {
                result = transactionalExecution.execute();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (Exception innerException) {
                    LOGGER.error("rollback failed", innerException);
                }
                throw e;
            }
            if (!readOnly) {
                connection.commit();
            }
            return result;
        } catch (SQLException e) {
            throw new DbApiException("Establish connection issue.", e);
        } finally {
            CURRENT_CONNECTION_HOLDER.set(oldConnection);
        }
    }

    @Nonnull
    public Connection getConnection() {
        Connection connection = CURRENT_CONNECTION_HOLDER.get();
        if (connection == null) {
            throw new IllegalStateException("Transaction is not active.");
        }
        return connection;
    }
}