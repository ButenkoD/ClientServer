package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ThreadLogHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;

class RequestHandler {
    private static final Logger logger = LogManager.getLogger(RequestHandler.class);

    void handle(PrintWriter responseStream, String request) {
        RequestHandler.DataProcessorThread thread = new DataProcessorThread(responseStream, request);
        thread.start();
        logger.debug("Started new thread. " + ThreadLogHelper.getThreadMessage());
    }

    private class DataProcessorThread extends Thread {
        private DataProcessor dataProcessor = new DataProcessor();
        private String dataToProcess;
        private PrintWriter responseStream;

        DataProcessorThread(PrintWriter responseStream, String dataToProcess) {
            this.responseStream = responseStream;
            this.dataToProcess = dataToProcess;
        }

        public void run() {
            try {
                sendResponse(dataProcessor.processData(dataToProcess));
                logger.debug("Data " + dataToProcess + " was processed. " + ThreadLogHelper.getThreadMessage());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        private void sendResponse(String message) throws Exception {
            responseStream.println(message);
        }
    }
}
