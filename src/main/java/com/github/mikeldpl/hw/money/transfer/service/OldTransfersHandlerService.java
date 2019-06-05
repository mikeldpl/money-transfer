package com.github.mikeldpl.hw.money.transfer.service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reject old transfers which was not handled in time
 */
public class OldTransfersHandlerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OldTransfersHandlerService.class);

    private final TransferActionService transferActionService;
    private final long transferExpirationPeriod;
    private final long executionPeriod;
    private final Timer timer;

    public OldTransfersHandlerService(TransferActionService transferActionService, long transferExpirationPeriod, long executionPeriod) {
        this.transferActionService = transferActionService;
        this.transferExpirationPeriod = transferExpirationPeriod;
        this.executionPeriod = executionPeriod;
        this.timer = new Timer("OldTransfersHandlerService_timer", true);
    }

    public void startScheduler() {
        Date firstExecutionDate = Date.from(ZonedDateTime.now().withMinute(0).withSecond(0).withSecond(0).toInstant());
        //todo: this scheduler need synchronization between nodes. But in context of test task this solution is just ok ;)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    transferActionService.rejectInProgressIdleTransfers(transferExpirationPeriod);
                } catch (Exception e) {
                    LOGGER.error("Error at job execution", e);
                }
            }
        }, firstExecutionDate, executionPeriod);
    }
}
