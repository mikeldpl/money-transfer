package com.github.mikeldpl.hw.money.transfer.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import com.github.mikeldpl.hw.money.transfer.exception.DbApiException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

@Singleton
public class DbUpdateService {

    private static final String CHANGE_LOG_FILE = "com/github/mikeldpl/hw/money/transfer/changelog.xml";
    private final DataSource dataSource;

    @Inject
    public DbUpdateService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void updateDbTables() {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(CHANGE_LOG_FILE, new ClassLoaderResourceAccessor(), database);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (SQLException | LiquibaseException e) {
            throw new DbApiException("Failed to update DB.", e);
        }
    }
}
