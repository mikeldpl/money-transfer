package com.github.mikeldpl.hw.money.transfer.model;

import com.google.gson.annotations.Expose;

public abstract class BaseModel {
    @Expose(deserialize = false)
    private Long id;
    @Expose(deserialize = false)
    private Long createdOn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }
}
