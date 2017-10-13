package com.andersenlab.client_server.udp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ThreadLogHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class RequestHandler {
    private static final Logger logger = LogManager.getLogger(RequestHandler.class);

    void handle(String request) {
        DataProcessorThread thread = new DataProcessorThread(request);
        thread.start();
        logger.debug("Started new thread. " + ThreadLogHelper.getThreadMessage());
    }

    private class DataProcessorThread extends Thread {
        private final static int responsePort = 22212;
        private final static String localhost = "127.0.0.1";
        private DataProcessor dataProcessor = new DataProcessor();
        private String dataToProcess;

        DataProcessorThread(String dataToProcess) {
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
            byte[] buf;
            DatagramPacket packet;
            buf = message.getBytes();
            packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(localhost), responsePort);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        }
    }
}
