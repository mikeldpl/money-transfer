package com.github.mikeldpl.hw.money.transfer.repository.jdbc;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.github.mikeldpl.hw.money.transfer.exception.DbApiException;

/**
 * Base helper class to use JDBC without pain. Executes INSERT statement.
 */
abstract class InsertExecutor {

    @Nonnull
    public final Long executeSingle(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(getQueryString(), Statement.RETURN_GENERATED_KEYS)) {
            fillParameter(statement);
            int result = statement.executeUpdate();
            if (result != 1) {
                throw new IllegalStateException("Insert command results in " + result + " rows modification.");
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getLong(1);
            }
        } catch (SQLException e) {
            throw new DbApiException("Insert execution issue. " + e.getMessage(), e);
        }
    }

    protected abstract void fillParameter(PreparedStatement statement) throws SQLException;

    @Nonnull
    protected abstract String getQueryString();
}
