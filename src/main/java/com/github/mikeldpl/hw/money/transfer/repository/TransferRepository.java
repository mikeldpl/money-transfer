package com.github.mikeldpl.hw.money.transfer.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.model.Transfer;

public interface TransferRepository extends BaseRepository<Transfer> {

    @Nonnull
    List<Transfer> getAllByAccountId(Long accountId);

    @Nullable
    Transfer getByAccountIdAndId(Long accountId, Long id);

    @Nullable
    Transfer getWithXLock(Long id);

    void updateStatus(Transfer model);
}
