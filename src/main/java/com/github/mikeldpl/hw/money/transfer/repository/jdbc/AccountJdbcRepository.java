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

import com.github.mikeldpl.hw.money.transfer.model.Account;
import com.github.mikeldpl.hw.money.transfer.repository.AccountRepository;
import com.github.mikeldpl.hw.money.transfer.service.TransactionExecutorJdbcService;

@Singleton
public class AccountJdbcRepository implements AccountRepository {

    private final TransactionExecutorJdbcService transactionExecutorJdbcService;


    @Inject
    public AccountJdbcRepository(TransactionExecutorJdbcService transactionExecutorJdbcService) {
        this.transactionExecutorJdbcService = transactionExecutorJdbcService;
    }

    @Nonnull
    @Override
    public Long add(Account model) {
        return new AccountInsertExecutor(model).executeSingle(transactionExecutorJdbcService.getConnection());
    }

    @Override
    public void updateMoney(Account model) {
        new AccountUpdateExecutor(model).executeSingle(transactionExecutorJdbcService.getConnection());
    }

    @Nonnull
    @Override
    public List<Account> getAll() {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new AccountSelectExecutor().execute(connection);
    }

    @Nullable
    @Override
    public Account getById(Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new AccountSelectExecutor("id = ?", id).executeSingle(connection);
    }

    @Nullable
    @Override
    public Account getWithXLock(Long id) {
        Connection connection = transactionExecutorJdbcService.getConnection();
        return new AccountSelectExecutor(true, "id = ?", id).executeSingle(connection);
    }

    private static class AccountUpdateExecutor extends UpdateExecutor {

        private final Account model;

        AccountUpdateExecutor(Account model) {
            this.model = model;
        }

        @Override
        protected @Nonnull
        String getQueryString() {
            return "UPDATE account SET money = ? WHERE id = ?";
        }

        @Override
        protected void fillParameter(PreparedStatement statement) throws SQLException {
            statement.setBigDecimal(1, model.getMoney());
            statement.setLong(2, model.getId());
        }
    }


    private static class AccountInsertExecutor extends InsertExecutor {

        private final Account model;

        private AccountInsertExecutor(Account model) {
            this.model = model;
        }

        @Override
        protected @Nonnull
        String getQueryString() {
            return "INSERT INTO account (name, money) VALUES (?, ?)";
        }

        @Override
        protected void fillParameter(PreparedStatement statement) throws SQLException {
            statement.setString(1, model.getName());
            statement.setBigDecimal(2, model.getMoney());
        }
    }


    private static class AccountSelectExecutor extends SelectExecutor<Account> {

        AccountSelectExecutor() {
        }

        AccountSelectExecutor(String filter, Object... params) {
            super(filter, params);
        }

        AccountSelectExecutor(boolean useLock, String filter, Object... params) {
            super(useLock, filter, params);
        }

        @Override
        @Nonnull
        protected String getQueryString() {
            return "SELECT id, created_on, name, money FROM account";
        }

        @Override
        @Nonnull
        protected Account rowToEntity(ResultSet resultSet) throws SQLException {
            Account account = new Account();
            account.setId(resultSet.getLong("id"));
            account.setCreatedOn(resultSet.getTimestamp("created_on").getTime());
            account.setName(resultSet.getString("name"));
            account.setMoney(resultSet.getBigDecimal("money"));
            return account;
        }
    }
}
