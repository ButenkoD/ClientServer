package com.andersenlab.client_server;

import java.util.Set;

public class ThreadLogHelper {
    public static String getThreadMessage()
    {
        return "Processed by thread #" + getThreadId() + ". Number of threads: " + getNumberOfThreads();
    }

    private static long getThreadId() {
        return Thread.currentThread().getId();
    }

    private static int getNumberOfThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        return threadSet.size();
    }
}
