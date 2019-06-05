package com.github.mikeldpl.hw.money.transfer.repository.jdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.model.Transfer;
import com.github.mikeldpl.hw.money.transfer.model.TransferStatus;
import com.github.mikeldpl.hw.money.transfer.repository.TransferRepository;
import com.github.mikeldpl.hw.money.transfer.service.TransactionExecutorJdbcService;
import org.jetbrains.annotations.NotNull;

@Singleton
public class TransferJdbcRepository implements TransferRepository {

    private final TransactionExecutorJdbcService transactionExecutorJdbcService;

    @Inject
    public TransferJdbcRepository(TransactionExecutorJdbcService transactionExecutorJdbcService) {
        this.transactionExecutorJdbcService = transactionExecutorJdbcService;
    }


    @NotNull
    @Override
    public List<Transfer> getAllByAccountId(Long accountId) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferSelectExecutor("sender_account_id = ?", accountId).execute(connection);
    }

    @Nullable
    @Override
    public Transfer getByAccountIdAndId(Long accountId, Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferSelectExecutor("sender_account_id = ? and id = ?", accountId, id).executeSingle(connection);
    }

    @Nullable
    @Override
    public Transfer getWithXLock(Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferSelectExecutor(true, "id = ?", id).executeSingle(connection);
    }

    @Nullable
    @Override
    public Transfer getById(Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferSelectExecutor("id = ?", id).executeSingle(connection);
    }

    @Nonnull
    @Override
    public Long add(Transfer model) {
        return new TransferInsertExecutor(model).executeSingle(transactionExecutorJdbcService.getConnection());
    }

    @Override
    public void updateStatus(Transfer model) {
        new TransferUpdateExecutor(model).executeSingle(transactionExecutorJdbcService.getConnection());
    }

    private static class TransferInsertExecutor extends InsertExecutor {

        private final Transfer model;

        private TransferInsertExecutor(Transfer model) {
            this.model = model;
        }

        @Override
        protected @Nonnull
        String getQueryString() {
            return "INSERT INTO transfer (status, sender_account_id, receiver_account_id, amount) VALUES (?, ?, ?, ?)";
        }

        @Override
        protected void fillParameter(PreparedStatement statement) throws SQLException {
            statement.setByte(1, (byte) model.getStatus().getId());
            statement.setLong(2, model.getSenderAccountId());
            statement.setLong(3, model.getReceiverAccountId());
            statement.setBigDecimal(4, model.getAmount());
        }
    }


    private static class TransferUpdateExecutor extends UpdateExecutor {

        private final Transfer model;

        TransferUpdateExecutor(Transfer model) {
            this.model = model;
        }

        @Override
        protected @Nonnull
        String getQueryString() {
            return "UPDATE transfer SET status = ? WHERE id = ?";
        }

        @Override
        protected void fillParameter(PreparedStatement statement) throws SQLException {
            statement.setByte(1, (byte) model.getStatus().getId());
            statement.setLong(2, model.getId());
        }
    }


    private static class TransferSelectExecutor extends SelectExecutor<Transfer> {

        TransferSelectExecutor(String filter, Object... params) {
            super(filter, params);
        }

        TransferSelectExecutor(boolean useLock, String filter, Object... params) {
            super(useLock, filter, params);
        }

        @Override
        @Nonnull
        protected String getQueryString() {
            return "SELECT id, created_on, status, sender_account_id, receiver_account_id, amount FROM transfer";
        }

        @Override
        @Nonnull
        protected Transfer rowToEntity(ResultSet resultSet) throws SQLException {
            Transfer transfer = new Transfer();
            transfer.setId(resultSet.getLong("id"));
            transfer.setCreatedOn(resultSet.getTimestamp("created_on").getTime());
            int statusId = resultSet.getByte("status");
            transfer.setStatus(TransferStatus.find(statusId));
            transfer.setSenderAccountId(resultSet.getLong("sender_account_id"));
            transfer.setReceiverAccountId(resultSet.getLong("receiver_account_id"));
            transfer.setAmount(resultSet.getBigDecimal("amount"));
            return transfer;
        }
    }
}
