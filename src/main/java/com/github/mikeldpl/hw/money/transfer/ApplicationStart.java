package com.github.mikeldpl.hw.money.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static spark.Spark.*;

public class ApplicationStart {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStart.class);

    public static void main(String[] args) {
        LOGGER.info("START");
        path("/money-transfer/1", () -> {
            get("/test", (request, response) -> "hello");
        });
    }
}
