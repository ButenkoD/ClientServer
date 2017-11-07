package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ThreadLogHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class RequestHandler {
    private static final int MAX_NUMBER_OF_THREADS = 10;
    private int numberOfThreads;
    private static final Logger logger = LogManager.getLogger(RequestHandler.class);

    void handle(Socket clientSocket) throws Exception {
        while (numberOfThreads >= MAX_NUMBER_OF_THREADS) {
            TimeUnit.SECONDS.sleep(1);
        }
        new DataProcessorThread(clientSocket).start();
        numberOfThreads++;
        logger.debug("RequestHandler has " + numberOfThreads + " thread(s).");
    }

    private class DataProcessorThread extends Thread {
        private Socket clientSocket;
        private DataProcessor dataProcessor = new DataProcessor();

        DataProcessorThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            try {
                BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String request = requestReader.readLine();
                if (request != null) {
                    logger.debug("Request " + request + " was received by Server. " + ThreadLogHelper.getThreadMessage());
                    sendResponse(dataProcessor.processData(request));
                    logger.debug("Sent response to " + request + " " + ThreadLogHelper.getThreadMessage());
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                logger.debug("RequestHandler has " + numberOfThreads + " thread(s).");
                numberOfThreads--;
            }
        }

        private void sendResponse(String message) throws Exception {
            PrintWriter responseStream = new PrintWriter(clientSocket.getOutputStream(), true);
            responseStream.println(message);
        }
    }
}
