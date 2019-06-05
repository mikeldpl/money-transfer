package com.github.mikeldpl.hw.money.transfer.repository.jdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.exception.DbApiException;

/**
 * Base helper class to use JDBC without pain. Executes SELECT statement.
 * @param <T> entity class to be selected
 */
abstract class SelectExecutor<T> {

    private final String filter;
    private final Object[] params;
    private final boolean useLock;

    SelectExecutor() {
        this(null);
    }

    SelectExecutor(String filter, Object... params) {
        this(false, filter, params);
    }

    SelectExecutor(boolean useLock, String filter, Object... params) {
        this.filter = filter;
        this.params = params;
        this.useLock = useLock;
    }

    @Nonnull
    public final List<T> execute(Connection connection) {
        String queryString = buildQueryString();
        try (PreparedStatement statement = connection.prepareStatement(queryString)) {
            fillParameter(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (resultSet.next()) {
                    T entity = rowToEntity(resultSet);
                    result.add(entity);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DbApiException("Select execution issue.", e);
        }
    }

    private void fillParameter(PreparedStatement statement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    @Nullable
    public final T executeSingle(Connection connection) {
        List<T> execute = execute(connection);
        if (execute.size() == 0) {
            return null;
        }
        if (execute.size() > 1) {
            throw new IllegalStateException("ResultSet contains more then one row.");
        }
        return execute.get(0);
    }


    @Nonnull
    private String buildQueryString() {
        StringBuilder stringBuilder = new StringBuilder(getQueryString());
        if (filter != null) {
            stringBuilder
                    .append(" WHERE ")
                    .append(filter);
        }
        if (useLock) {
            stringBuilder.append(" FOR UPDATE");
        }
        return stringBuilder.toString();
    }

    @Nonnull
    protected abstract String getQueryString();

    @Nonnull
    protected abstract T rowToEntity(ResultSet resultSet) throws SQLException;

}
