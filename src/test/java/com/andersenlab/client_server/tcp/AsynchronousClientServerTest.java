package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.ClientInterface;
import com.andersenlab.client_server.ClientServerBaseTest;
import com.andersenlab.client_server.ServerInterface;

public class AsynchronousClientServerTest extends ClientServerBaseTest {
    @Override
    protected ServerInterface getServer() {
        return new AsynchronousServer();
    }
    @Override
    protected ClientInterface getClient() {
        return new Client();
    }
}
