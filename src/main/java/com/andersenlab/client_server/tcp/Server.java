package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.net.ServerSocket;

public class Server implements ServerInterface {
    private static final int port = 22211;
    private static final Logger logger = LogManager.getLogger(Server.class);
    private ServerSocket serverSocket;
    private RequestHandler requestHandler = new RequestHandler();

    public static void main(String[] args) {
        new Server().listen();
    }

    public void listen() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                requestHandler.handle(serverSocket.accept());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            safelyClose(serverSocket);
        }
    }

    public void stop() {
        safelyClose(serverSocket);
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
