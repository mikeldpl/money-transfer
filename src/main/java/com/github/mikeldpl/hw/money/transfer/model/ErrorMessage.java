package com.github.mikeldpl.hw.money.transfer.model;

import com.google.gson.annotations.Expose;

public class ErrorMessage {
    @Expose
    private final String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
