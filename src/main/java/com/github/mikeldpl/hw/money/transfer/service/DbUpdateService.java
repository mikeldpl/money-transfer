package com.github.mikeldpl.hw.money.transfer.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

@Singleton
public class DbUpdateService {

    private final DataSource dataSource;

    @Inject
    public DbUpdateService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrateDb() {
        //executes SQL from db/migration
        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
    }
}
