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

import com.github.mikeldpl.hw.money.transfer.model.TransferAction;
import com.github.mikeldpl.hw.money.transfer.model.TransferStatus;
import com.github.mikeldpl.hw.money.transfer.repository.TransferActionRepository;
import com.github.mikeldpl.hw.money.transfer.service.TransactionExecutorJdbcService;

@Singleton
public class TransferActionJdbcRepository implements TransferActionRepository {

    private final TransactionExecutorJdbcService transactionExecutorJdbcService;

    @Inject
    public TransferActionJdbcRepository(TransactionExecutorJdbcService transactionExecutorJdbcService) {
        this.transactionExecutorJdbcService = transactionExecutorJdbcService;
    }

    @Nonnull
    @Override
    public Long add(TransferAction model) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferActionInsertExecutor(model).executeSingle(connection);
    }

    @Nullable
    @Override
    public TransferAction getById(Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferActionSelectExecutor("id = ?", id).executeSingle(connection);
    }

    @Nonnull
    @Override
    public List<TransferAction> getAllByTransferId(Long transferId) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferActionSelectExecutor("transfer_id = ?", transferId).execute(connection);
    }

    @Nullable
    @Override
    public TransferAction getByTransferIdAndId(Long transferId, Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new TransferActionSelectExecutor("transfer_id = ? and id = ?", transferId, id).executeSingle(connection);
    }

    private static class TransferActionInsertExecutor extends InsertExecutor {

        private final TransferAction model;

        private TransferActionInsertExecutor(TransferAction model) {
            this.model = model;
        }

        @Override
        protected @Nonnull
        String getQueryString() {
            return "INSERT INTO transfer_action (reason, next_status, transfer_id) VALUES (?, ?, ?)";
        }

        @Override
        protected void fillParameter(PreparedStatement statement) throws SQLException {
            statement.setString(1, model.getReason());
            statement.setByte(2, (byte) model.getNextStatus().getId());
            statement.setLong(3, model.getTransferId());
        }
    }


    private static class TransferActionSelectExecutor extends SelectExecutor<TransferAction> {

        TransferActionSelectExecutor(String filter, Object... params) {
            super(filter, params);
        }

        @Override
        @Nonnull
        protected String getQueryString() {
            return "SELECT id, created_on, reason, next_status, transfer_id FROM transfer_action";
        }

        @Override
        @Nonnull
        protected TransferAction rowToEntity(ResultSet resultSet) throws SQLException {
            TransferAction transferAction = new TransferAction();
            transferAction.setId(resultSet.getLong("id"));
            transferAction.setCreatedOn(resultSet.getTimestamp("created_on").getTime());
            transferAction.setReason(resultSet.getString("reason"));
            int nextStatusId = resultSet.getByte("next_status");
            transferAction.setNextStatus(TransferStatus.find(nextStatusId));
            transferAction.setTransferId(resultSet.getLong("transfer_id"));
            return transferAction;
        }
    }
}
