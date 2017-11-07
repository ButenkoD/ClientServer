package com.andersenlab.client_server;

import java.util.List;

public interface ClientInterface {
    List<String> sendMessagesAndGetResponses(String[] messages) throws Exception;
}
