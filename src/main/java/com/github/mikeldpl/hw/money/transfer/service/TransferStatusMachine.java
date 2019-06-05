package com.github.mikeldpl.hw.money.transfer.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.mikeldpl.hw.money.transfer.exception.ValidationApiException;
import com.github.mikeldpl.hw.money.transfer.model.Transfer;
import com.github.mikeldpl.hw.money.transfer.model.TransferStatus;

@Singleton
public class TransferStatusMachine {
    private final Map<StatusChange, Consumer<Transfer>> stateMachineDeclaration = new LinkedHashMap<>();

    @Inject
    public TransferStatusMachine(AccountService accountService) {
        initStateMachine(accountService);
    }

    private void initStateMachine(AccountService accountService) {
        //new -> PROCESSING
        stateMachineDeclaration.put(StatusChange.of(null, TransferStatus.PROCESSING), (transfer) -> {
            BigDecimal negateAmount = transfer.getAmount().negate();
            accountService.addAccountMoney(transfer.getSenderAccountId(), negateAmount);
        });
        //PROCESSING -> APPROVED
        stateMachineDeclaration.put(StatusChange.of(TransferStatus.PROCESSING, TransferStatus.APPROVED), (transfer) -> {
            BigDecimal amount = transfer.getAmount();
            Long receiverAccountId = transfer.getReceiverAccountId();
            accountService.addAccountMoney(receiverAccountId, amount);
        });
        //PROCESSING -> APPROVED
        stateMachineDeclaration.put(StatusChange.of(TransferStatus.PROCESSING, TransferStatus.REJECTED), (transfer) -> {
            BigDecimal amount = transfer.getAmount();
            Long senderAccountId = transfer.getSenderAccountId();
            accountService.addAccountMoney(senderAccountId, amount);
        });
    }

    public void handleStatusChange(Transfer transfer, TransferStatus newStatus) {
        StatusChange statusChange = StatusChange.of(transfer.getStatus(), newStatus);
        Consumer<Transfer> transferConsumer = stateMachineDeclaration.get(statusChange);
        if (transferConsumer == null) {
            throw new ValidationApiException("Unsupported status change: " + statusChange
                                                     + ". Supported status changes: " + stateMachineDeclaration.keySet());
        }
        transferConsumer.accept(transfer);
    }

    private static class StatusChange {
        private final TransferStatus oldStatus;
        private final TransferStatus newStatus;

        private StatusChange(TransferStatus oldStatus, TransferStatus newStatus) {
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
        }

        static StatusChange of(TransferStatus oldStatus, TransferStatus newStatus) {
            return new StatusChange(oldStatus, newStatus);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StatusChange)) {
                return false;
            }
            StatusChange that = (StatusChange) o;
            return oldStatus == that.oldStatus &&
                    newStatus == that.newStatus;
        }

        @Override
        public int hashCode() {
            return Objects.hash(oldStatus, newStatus);
        }

        @Override
        public String toString() {
            return oldStatus + " -> " + newStatus;
        }
    }
}
