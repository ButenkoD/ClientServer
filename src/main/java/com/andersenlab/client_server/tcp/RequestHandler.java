package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ThreadLogHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RequestHandler {
    private static final int MAX_NUMBER_OF_THREADS = 10;
    private static final Logger logger = LogManager.getLogger(RequestHandler.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);

    void handle(Socket clientSocket) throws Exception {
        executorService.execute(() -> {
            DataProcessor dataProcessor = new DataProcessor();
            try (
                    BufferedReader requestReader =
                            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String request = requestReader.readLine();
                if (request != null) {
                    logger.debug("Request " + request + " was received by Server. " + ThreadLogHelper.getThreadMessage());
                    sendResponse(clientSocket, dataProcessor.processData(request));
                    logger.debug("Sent response to " + request + " " + ThreadLogHelper.getThreadMessage());
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (Throwable throwable) {
                    logger.debug("Can't close clientSocket");
                }
            }
        });
    }

    private void sendResponse(Socket clientSocket, String message) throws Exception {
        try (PrintWriter responseStream = new PrintWriter(clientSocket.getOutputStream(), true)) {
            responseStream.println(message);
        }
    }

    void stop() {
        executorService.shutdown();
    }
}
