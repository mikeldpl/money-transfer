package com.github.mikeldpl.hw.money.transfer;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;

import com.github.mikeldpl.hw.money.transfer.repository.AccountRepository;
import com.github.mikeldpl.hw.money.transfer.repository.TransferActionRepository;
import com.github.mikeldpl.hw.money.transfer.repository.TransferRepository;
import com.github.mikeldpl.hw.money.transfer.repository.jdbc.AccountJdbcRepository;
import com.github.mikeldpl.hw.money.transfer.repository.jdbc.TransferActionJdbcRepository;
import com.github.mikeldpl.hw.money.transfer.repository.jdbc.TransferJdbcRepository;
import com.github.mikeldpl.hw.money.transfer.service.TransactionExecutorJdbcService;
import com.github.mikeldpl.hw.money.transfer.service.TransactionExecutorService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import org.hsqldb.jdbc.JDBCDataSource;

@Module
abstract class AppModule {
    private static final int TRANSACTION_ISOLATION = Connection.TRANSACTION_READ_COMMITTED;

    @Provides
    @Singleton
    static DataSource dataSource() {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setURL("jdbc:hsqldb:mem:money_transfer");
        //todo: move to external config
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Provides
    @Singleton
    static Gson gson() {
        return new GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides
    @Singleton
    static TransactionExecutorJdbcService transactionExecutorJdbcService(DataSource dataSource) {
        return new TransactionExecutorJdbcService(dataSource, TRANSACTION_ISOLATION);
    }

    @Binds
    abstract AccountRepository accountRepository(AccountJdbcRepository accountJdbcRepository);

    @Binds
    abstract TransactionExecutorService transactionExecutorService(TransactionExecutorJdbcService transactionExecutorJdbcService);

    @Binds
    abstract TransferRepository transferRepository(TransferJdbcRepository accountJdbcRepository);

    @Binds
    abstract TransferActionRepository transferActionRepository(TransferActionJdbcRepository transferActionJdbcRepository);
}
