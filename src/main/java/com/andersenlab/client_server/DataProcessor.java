package com.andersenlab.client_server;

import java.util.concurrent.TimeUnit;

public class DataProcessor {
    private static final int timeout = 1;

    public String processData(String data) throws Exception {
        TimeUnit.SECONDS.sleep(timeout);
        return "'" + data + "'" + " is processed by " + Thread.currentThread().getId();
    }
}
