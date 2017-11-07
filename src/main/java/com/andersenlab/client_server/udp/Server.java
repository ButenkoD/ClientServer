package com.andersenlab.client_server.udp;

import com.andersenlab.client_server.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server implements ServerInterface {
    private final static int port = 22211;
    private static final int bufferSize = 1024;
    private static final Logger logger = LogManager.getLogger(Server.class);
    private RequestHandler requestHandler = new RequestHandler();
    private boolean stop = false;

    public void listen() {
        byte[] buf = new byte[bufferSize];
        DatagramSocket socket = null;
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket = new DatagramSocket(port);
            while (!stop) {
                socket.receive(packet);
                requestHandler.handle(packet);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public void stop() {
        stop = true;
    }
}
