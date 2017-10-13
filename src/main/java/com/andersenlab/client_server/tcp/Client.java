package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.ClientInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements ClientInterface {
    private static final int port = 22211;
    private static final String localhost = "127.0.0.1";
    private static final Logger logger = LogManager.getLogger(Client.class);

    public static void main(String[] args) {
        String[] mesages = {"aaa", "bbb", "bbb"};
        for (String response: new Client().sendMessagesAndGetResponses(mesages)) {
            System.out.println(response);
        }
    }

    public String[] sendMessagesAndGetResponses(String[] messages) {
        Socket socket = null;
        try {
            socket = new Socket(localhost, port);
            sendMessages(socket, messages);
            return getResponses(socket, messages.length);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            safelyClose(socket);
        }
    }

    private void sendMessages(Socket socket, String[] messages) throws Exception {
        PrintWriter requestWriter = new PrintWriter(socket.getOutputStream());
        for (String message: messages) {
            requestWriter.println(message);
        }
        requestWriter.flush();
    }

    private String[] getResponses(Socket socket, int numberOfResponses) throws Exception {
        BufferedReader responseReader = null;
        try {
            String[] responses = new String[numberOfResponses];
            responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            boolean receivedAllResponses = false;
            String response;
            int i = 0;
            while (!receivedAllResponses) {
                response = responseReader.readLine();
                if (response != null) {
                    responses[i++] = response;
                    if (i == numberOfResponses) {
                        receivedAllResponses = true;
                    }
                }
            }
            return responses;
        } finally {
            safelyClose(responseReader);
        }
    }

    private void safelyClose(Closeable closeable) {
        try {
            if (closeable != null) {
                System.out.println("Closeable " + closeable.getClass().getSimpleName() + " was closed");
                closeable.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
