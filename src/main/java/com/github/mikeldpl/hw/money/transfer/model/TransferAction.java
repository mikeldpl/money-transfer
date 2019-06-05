package com.github.mikeldpl.hw.money.transfer.model;

import com.google.gson.annotations.Expose;

public class TransferAction extends BaseModel {

    @Expose
    private TransferStatus nextStatus;
    @Expose
    private String reason;
    @Expose(deserialize = false)
    private Long transferId;

    public TransferStatus getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(TransferStatus nextStatus) {
        this.nextStatus = nextStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }
}
