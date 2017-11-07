package com.andersenlab.client_server.udp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ThreadLogHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

class RequestHandler {
    private static final int MAX_NUMBER_OF_THREADS = 10;
    private int numberOfThreads;
    private static final Logger logger = LogManager.getLogger(RequestHandler.class);

    void handle(DatagramPacket packet) throws Exception {
        while (numberOfThreads >= MAX_NUMBER_OF_THREADS) {
            TimeUnit.SECONDS.sleep(1);
        }
        DataProcessorThread thread = new DataProcessorThread(packet);
        thread.start();
        numberOfThreads++;
        logger.debug("Started new thread. " + ThreadLogHelper.getThreadMessage());
    }

    private class DataProcessorThread extends Thread {
        private final static int responsePort = 22212;
        private final static String localhost = "127.0.0.1";
        private DataProcessor dataProcessor = new DataProcessor();
        private String dataToProcess;

        DataProcessorThread(DatagramPacket packet) {
            this.dataToProcess = new String(packet.getData(), 0, packet.getLength());
        }

        public void run() {
            try {
                sendResponse(dataProcessor.processData(dataToProcess));
                logger.debug("Data " + dataToProcess + " was processed. " + ThreadLogHelper.getThreadMessage());
            } catch (Exception e) {
                logger.error(e.getMessage());
            } finally {
                numberOfThreads--;
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
