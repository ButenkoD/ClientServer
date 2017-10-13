package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.ServerInterface;
import com.andersenlab.client_server.ThreadLogHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements ServerInterface {
    private static final int port = 22211;
    private static final Logger logger = LogManager.getLogger(Server.class);
    private boolean stop = false;
    private RequestHandler requestHandler = new RequestHandler();

    public static void main(String[] args) {
        new Server().listen();
    }

    public void listen() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            PrintWriter responseStream = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request;
            while(!stop) {
                request = requestReader.readLine();
                if (request != null) {
                    logger.debug("Request " + request + " was received by Server. " + ThreadLogHelper.getThreadMessage());
                    requestHandler.handle(responseStream, request);
                    logger.debug("Sent response to " + request + " " + ThreadLogHelper.getThreadMessage());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            safelyClose(serverSocket);
        }
    }

    public void stop() {
        stop = true;
    }

    private void safelyClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
