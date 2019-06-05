package com.github.mikeldpl.hw.money.transfer.service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.exception.NotFoundApiException;
import com.github.mikeldpl.hw.money.transfer.exception.ValidationApiException;
import com.github.mikeldpl.hw.money.transfer.model.Transfer;
import com.github.mikeldpl.hw.money.transfer.model.TransferStatus;
import com.github.mikeldpl.hw.money.transfer.repository.TransferRepository;

@Singleton
public class TransferService {

    private final TransactionExecutorService transactionExecutorService;
    private final TransferRepository transferRepository;
    private final AccountService accountService;
    private final TransferStatusMachine transferStatusMachine;

    @Inject
    public TransferService(TransactionExecutorService transactionExecutorService,
                           TransferRepository transferRepository, AccountService accountService,
                           TransferStatusMachine transferStatusMachine) {
        this.transactionExecutorService = transactionExecutorService;
        this.transferRepository = transferRepository;
        this.accountService = accountService;
        this.transferStatusMachine = transferStatusMachine;
    }

    @Nonnull
    public List<Transfer> getAllByAccountId(Long accountId) {
        return transactionExecutorService.executeTransaction(true, () -> {
            accountService.checkIfAccountExists(accountId);
            return transferRepository.getAllByAccountId(accountId);
        });
    }

    @Nonnull
    public Transfer createTransfer(Long accountId, Transfer transfer) {
        transfer.setSenderAccountId(accountId);
        validateEntity(transfer);

        return transactionExecutorService.executeTransaction(false, () -> {
            transferStatusMachine.handleStatusChange(transfer, TransferStatus.PROCESSING);
            transfer.setStatus(TransferStatus.PROCESSING);
            Long id = transferRepository.add(transfer);
            return transferRepository.getById(id);
        });
    }

    public void changeStatus(Long transferId, TransferStatus nextStatus) {
        transactionExecutorService.executeTransaction(false, () -> {
            Transfer transfer = transferRepository.getWithXLock(transferId);
            checkExistence(transferId, transfer);

            transferStatusMachine.handleStatusChange(transfer, nextStatus);
            transfer.setStatus(nextStatus);
            transferRepository.updateStatus(transfer);
            return transfer;
        });
    }

    private void validateEntity(Transfer transfer) {
        Long receiverAccountId = transfer.getReceiverAccountId();
        if (receiverAccountId == null) {
            throw new ValidationApiException("Receiver account id is required.");
        }
        Long senderAccountId = transfer.getSenderAccountId();
        if (senderAccountId == null) {
            throw new ValidationApiException("Sender account id is required.");
        }
        if (senderAccountId.equals(receiverAccountId)) {
            throw new ValidationApiException("Receiver and sender account can not be the same.");
        }

        BigDecimal amount = transfer.getAmount();
        if (amount == null) {
            throw new ValidationApiException("Amount is required.");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationApiException("Amount can not be less the zero.");
        }
    }

    public Transfer getByAccountIdAndId(Long accountId, Long id) {
        Transfer transfer = transactionExecutorService.executeTransaction(true, () -> {
            accountService.checkIfAccountExists(accountId);
            return transferRepository.getByAccountIdAndId(accountId, id);
        });
        if (transfer == null) {
            throw new NotFoundApiException("Transfer is not found. Account id: " + accountId + ", id: " + id);
        }
        return transfer;
    }

    private void checkExistence(Long id, Transfer transfer) {
        if (transfer == null) {
            throw new NotFoundApiException("Transfer is not found. Id: " + id);
        }
    }

    public void checkIfTransferExists(Long accountId, Long transferId) {
        //todo: use count
        getByAccountIdAndId(accountId, transferId);
    }
}
