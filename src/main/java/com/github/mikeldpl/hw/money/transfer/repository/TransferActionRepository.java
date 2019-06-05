package com.github.mikeldpl.hw.money.transfer.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.model.TransferAction;

public interface TransferActionRepository extends BaseRepository<TransferAction> {
    @Nonnull
    List<TransferAction> getAllByTransferId(Long transferId);

    @Nullable
    TransferAction getByTransferIdAndId(Long transferId, Long id);
}
