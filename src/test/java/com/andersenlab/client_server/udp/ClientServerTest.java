package com.andersenlab.client_server.udp;

import com.andersenlab.client_server.ClientInterface;
import com.andersenlab.client_server.ClientServerBaseTest;
import com.andersenlab.client_server.ServerInterface;

public class ClientServerTest extends ClientServerBaseTest {
    @Override
    protected ServerInterface getServer() {
        return new Server();
    }
    @Override
    protected ClientInterface getClient() {
        return new Client();
    }
}
