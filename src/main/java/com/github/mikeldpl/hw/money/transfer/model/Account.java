package com.github.mikeldpl.hw.money.transfer.model;

import java.math.BigDecimal;

import com.google.gson.annotations.Expose;

public class Account extends BaseModel {
    @Expose
    private String name;
    @Expose
    private BigDecimal money;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
}
