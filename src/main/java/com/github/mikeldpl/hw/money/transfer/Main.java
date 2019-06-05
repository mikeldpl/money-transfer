package com.github.mikeldpl.hw.money.transfer;

public class Main {

    public static void main(String[] args) {
        MoneyTransferRestServer moneyTransferRestServer = DaggerAppComponent.create().build();
        moneyTransferRestServer.run();
    }
}
