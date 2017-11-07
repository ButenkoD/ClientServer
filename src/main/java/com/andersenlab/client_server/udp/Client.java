package com.andersenlab.client_server.udp;

import com.andersenlab.client_server.ClientInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Client implements ClientInterface {
    private static final int sendPort = 22211;
    private static final int responsePort = 22212;
    private static final int bufferSize = 1024;
    private static final String localhost = "127.0.0.1";
    private static final Logger logger = LogManager.getLogger(Client.class);

    public List<String> sendMessagesAndGetResponses(String[] messages) {
        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket(responsePort);
            byte[] receiveData = new byte[bufferSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            List<String> responses = new ArrayList<>();
            sendMessages(messages);
            for (int i = 0; i < messages.length; i++) {
                clientSocket.receive(receivePacket);
                responses.add(i, new String(receivePacket.getData(), 0, receivePacket.getLength()));
            }
            return responses;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    private void sendMessages(String[] messages) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf;
            DatagramPacket packet;
            for (String message: messages) {
                buf = message.getBytes();
                packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(localhost), sendPort);
                socket.send(packet);
            }
            socket.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
