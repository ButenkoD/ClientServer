package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.ClientInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements ClientInterface {
    private static final int port = 22211;
    private static final String localhost = "127.0.0.1";
    private static final Logger logger = LogManager.getLogger(Client.class);
    private static List<String> responses = new ArrayList<>();

    public static void main(String[] args) {
        String[] mesages = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg", "hhh", "iii"};
        for (String response: new Client().sendMessagesAndGetResponses(mesages)) {
            System.out.println(response);
        }
    }

    public List<String> sendMessagesAndGetResponses(String[] messages) {
        try {
            sendMessages(messages);
            return responses;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private void sendMessages(String[] messages) throws Exception {
        List<Socket> sockets = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            sockets.add(i, makeRequestAndGetSocket(messages[i]));
        }
        for (int i = 0; i < sockets.size(); i++) {
            responses.add(i, getResponse(sockets.get(i)));
        }
    }

    private Socket makeRequestAndGetSocket(String message) throws Exception {
        Socket socket = new Socket(localhost, port);
        PrintWriter requestWriter = new PrintWriter(socket.getOutputStream());
        logger.debug("Sent request: " + message);
        requestWriter.println(message);
        requestWriter.flush();
        return socket;
    }

    private String getResponse(Socket socket) {
        BufferedReader responseReader = null;
        try {
            responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return responseReader.readLine();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            safelyClose(responseReader);
            safelyClose(socket);
        }
    }

    private void safelyClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
                System.out.println("Closeable " + closeable.getClass().getSimpleName() + " was closed");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
