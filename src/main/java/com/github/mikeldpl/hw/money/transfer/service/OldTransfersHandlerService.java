package com.github.mikeldpl.hw.money.transfer.service;

import javax.inject.Singleton;

/**
 * Reject old transfers which was not handled in time
 */
@Singleton
public class OldTransfersHandlerService {

    private final long transferExpirationPeriod;
    private final String cron;

    public OldTransfersHandlerService(long transferExpirationPeriod, String cron) {
        this.transferExpirationPeriod = transferExpirationPeriod;
        this.cron = cron;
    }

    public void startScheduler() {
        //todo
    }
}
