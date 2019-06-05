package com.github.mikeldpl.hw.money.transfer.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import com.github.mikeldpl.hw.money.transfer.model.Account;

public interface AccountRepository extends BaseRepository<Account> {

    @Nullable
    Account getWithXLock(Long id);

    void updateMoney(Account model);

    @Nonnull
    List<Account> getAll();
}
