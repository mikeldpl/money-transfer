package com.github.mikeldpl.hw.money.transfer.model;

import java.math.BigDecimal;

import com.google.gson.annotations.Expose;

public class Transfer extends BaseModel {

    @Expose(deserialize = false)
    private TransferStatus status;
    @Expose(deserialize = false)
    private Long senderAccountId;
    @Expose
    private Long receiverAccountId;
    @Expose
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public Long getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(Long senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public Long getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(Long receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }
}
