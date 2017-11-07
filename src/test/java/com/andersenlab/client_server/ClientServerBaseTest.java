package com.andersenlab.client_server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public abstract class ClientServerBaseTest {
    private ServerInterface server;

    protected abstract ServerInterface getServer();
    protected abstract ClientInterface getClient();

    @Before
    public void runServer() throws Exception {
        new Thread(() -> {
                server = getServer();
                server.listen();
        }).start();
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void testClientServer() throws Exception {
        String[] messages = getMessages();
        List<String> responses = getClient().sendMessagesAndGetResponses(messages);
        for (int i = 0; i < messages.length; i++) {
            assertThat(responses.get(i), containsString("'"+messages[i]+"' is processed"));
        }
    }

    @After
    public void stopServer() {
        server.stop();
        System.out.println(ThreadLogHelper.getThreadMessage());
    }

    private String[] getMessages() {
        List<String> messages = new ArrayList<>();
        String[] initMessages = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg", "hhh", "iii", "jjj"};
        for (String initMessage: initMessages) {
            for (int i = 0; i < 100; i++) {
                messages.add(initMessage + Integer.toString(i));
            }
        }
        return messages.toArray(new String[0]);
    }
}
