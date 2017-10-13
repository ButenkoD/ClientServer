package com.andersenlab.client_server;

public interface ClientInterface {
    String[] sendMessagesAndGetResponses(String[] messages) throws Exception;
}
