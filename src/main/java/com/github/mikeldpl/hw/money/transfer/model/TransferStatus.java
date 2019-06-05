package com.github.mikeldpl.hw.money.transfer.model;

public enum TransferStatus {
    PROCESSING(1), APPROVED(2), REJECTED(3);

    private final int id;

    TransferStatus(int id) {
        this.id = id;
    }

    public static TransferStatus find(int statusId) {
        for (TransferStatus value : TransferStatus.values()) {
            if (value.id == statusId) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }

    public int getId() {
        return id;
    }
}
