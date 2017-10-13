package com.andersenlab.client_server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public abstract class ClientServerBaseTest {
    private ServerInterface server;
    private Thread thread;

    protected abstract ServerInterface getServer();
    protected abstract ClientInterface getClient();

    @Before
    public void runServer() throws Exception {
        thread = new Thread(new Runnable() {
            public void run() {
                server = getServer();
                server.listen();
            }
        });
        thread.start();
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void testUdp() throws Exception {
        String[] messages = {"QWE", "ASD", "ZXC"};
        String[] responses = getClient().sendMessagesAndGetResponses(messages);
        for (int i = 0; i < messages.length; i++) {
            assertThat(responses[i], containsString("'"+messages[i]+"' is processed"));
        }
    }

    @After
    public void stopServer() {
        server.stop();
        thread.interrupt();
        System.out.println(ThreadLogHelper.getThreadMessage());
    }
}
