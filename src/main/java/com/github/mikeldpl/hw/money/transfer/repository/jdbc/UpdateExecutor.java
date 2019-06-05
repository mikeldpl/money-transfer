package com.github.mikeldpl.hw.money.transfer.repository.jdbc;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.github.mikeldpl.hw.money.transfer.exception.DbApiException;

/**
 * Base helper class to use JDBC without pain. Executes UPDATE statement.
 */
abstract class UpdateExecutor {

    public final void executeSingle(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(getQueryString())) {
            fillParameter(statement);
            int result = statement.executeUpdate();
            if (result != 1) {
                throw new IllegalStateException("Update command results in " + result + " rows modification.");
            }
        } catch (SQLException e) {
            throw new DbApiException("Update execution issue. " + e.getMessage(), e);
        }
    }

    protected abstract void fillParameter(PreparedStatement statement) throws SQLException;

    @Nonnull
    protected abstract String getQueryString();
}
