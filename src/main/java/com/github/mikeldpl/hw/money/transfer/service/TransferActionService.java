package com.github.mikeldpl.hw.money.transfer.service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.exception.NotFoundApiException;
import com.github.mikeldpl.hw.money.transfer.exception.ValidationApiException;
import com.github.mikeldpl.hw.money.transfer.model.TransferAction;
import com.github.mikeldpl.hw.money.transfer.repository.TransferActionRepository;

@Singleton
public class TransferActionService {

    private final TransactionExecutorService transactionExecutorService;
    private final TransferActionRepository transferActionRepository;
    private final TransferService transferService;

    @Inject
    public TransferActionService(TransactionExecutorService transactionExecutorService,
                                 TransferActionRepository transferActionRepository,
                                 TransferService transferService) {
        this.transactionExecutorService = transactionExecutorService;
        this.transferActionRepository = transferActionRepository;
        this.transferService = transferService;
    }

    @Nonnull
    public List<TransferAction> getAllByTransferId(Long accountId, Long transferId) {
        return transactionExecutorService.executeTransaction(true, () -> {
            transferService.checkIfTransferExists(accountId, transferId);
            return transferActionRepository.getAllByTransferId(transferId);
        });
    }

    @Nonnull
    public TransferAction createTransferAction(Long accountId, Long transferId, TransferAction transferAction) {
        transferAction.setTransferId(transferId);
        validateEntity(transferAction);
        return transactionExecutorService.executeTransaction(false, () -> {
            transferService.checkIfTransferExists(accountId, transferId);
            //grabs X lock for transfer.
            transferService.changeStatus(transferId, transferAction.getNextStatus());
            Long id = transferActionRepository.add(transferAction);
            return transferActionRepository.getById(id);
        });
    }

    private void validateEntity(TransferAction transferAction) {
        if (transferAction.getNextStatus() == null) {
            throw new ValidationApiException("Next status is required.");
        }
    }

    @Nonnull
    public TransferAction getByTransferIdAndId(Long accountId, Long transferId, Long id) {
        TransferAction transferAction = transactionExecutorService.executeTransaction(true, () -> {
            transferService.checkIfTransferExists(accountId, transferId);
            return transferActionRepository.getByTransferIdAndId(transferId, id);
        });
        if (transferAction == null) {
            throw new NotFoundApiException("Action is not found. Transfer id: " + transferId + ", id: " + id);
        }
        return transferAction;
    }
}
