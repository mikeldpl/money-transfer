package com.github.mikeldpl.hw.money.transfer.service;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.exception.NotFoundApiException;
import com.github.mikeldpl.hw.money.transfer.exception.ValidationApiException;
import com.github.mikeldpl.hw.money.transfer.model.Transfer;
import com.github.mikeldpl.hw.money.transfer.model.TransferAction;
import com.github.mikeldpl.hw.money.transfer.model.TransferStatus;
import com.github.mikeldpl.hw.money.transfer.repository.TransferActionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TransferActionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferActionService.class);

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
            return changeStatusAndPersist(transferId, transferAction);
        });
    }

    private TransferAction changeStatusAndPersist(Long transferId, TransferAction transferAction) {
        //grabs X lock for transfer.
        transferService.changeStatus(transferId, transferAction.getNextStatus());
        Long id = transferActionRepository.add(transferAction);
        return transferActionRepository.getById(id);
    }

    public void rejectInProgressIdleTransfers(long transferExpirationPeriod) {
        Instant expirationBorder = Instant.now().minusMillis(transferExpirationPeriod);

        Integer count = transactionExecutorService.executeTransaction(false, () -> {
            List<Transfer> expiredTransfers = transferService.getByStatusAndCreateDateLimitWithXLock(expirationBorder, TransferStatus.PROCESSING);
            for (Transfer expiredTransfer : expiredTransfers) {
                transferService.changeStatus(expiredTransfer, TransferStatus.REJECTED);
            }
            return expiredTransfers.size();
        });
        LOGGER.info("Removed idle transfers: {}", count);
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
