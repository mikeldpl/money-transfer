package com.github.mikeldpl.hw.money.transfer;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    MoneyTransferRestServer build();
}
