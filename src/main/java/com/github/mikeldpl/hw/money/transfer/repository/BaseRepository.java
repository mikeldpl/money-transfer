package com.github.mikeldpl.hw.money.transfer.repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BaseRepository<T> {

    /**
     * Creates a new record in a storage.
     * @param model to be persisted
     */
    @Nonnull
    Long add(T model);

    /**
     * Selects record by identifier.
     * @param id identifier
     */
    @Nullable
    T getById(Long id);
}
