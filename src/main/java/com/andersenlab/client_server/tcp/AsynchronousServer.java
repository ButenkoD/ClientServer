package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsynchronousServer implements ServerInterface {
    private static final int port = 22211;
    private static final Logger logger = LogManager.getLogger(Server.class);
    private final DataProcessor dataProcessor = new DataProcessor();
    private static final int MAX_NUMBER_OF_THREADS = 10;
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS);
    private Selector selector;
    private boolean isListening;

    public static void main(String[] args) {
        new AsynchronousServer().listen();
    }

    public void listen() {
        if (!isListening) {
            isListening = true;
            try (
                    Selector selector = Selector.open();
                    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()
            ) {
                this.selector = selector;
                initServerSocketChannel(serverSocketChannel);
                performListening(serverSocketChannel);

            } catch (Exception exception) {
                logger.error(exception);
            }
        }
    }

    public void stop() {
        if (isListening) {
            isListening = false;
            executorService.shutdown();
            try {
                if (selector != null) {
                    selector.close();
                }
            } catch (Exception exception) {
                logger.error("Can't close selectot");
            }
        }
    }


    private void initServerSocketChannel(ServerSocketChannel serverSocketChannel) throws Exception {
        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void performListening(ServerSocketChannel serverSocketChannel) throws Exception {
        while (selector.isOpen()) {
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey selectionKey = selectedKeys.next();

                if (selectionKey.isValid()) {
                    if (selectionKey.isAcceptable()) {
                        registerAsReadable(serverSocketChannel);
                    }

                    if (selectionKey.isReadable()) {
                        readAndAnswer(selectionKey);
                    }
                }
                selectedKeys.remove();
            }
        }
        logger.debug("Selector was closed");
    }

    private void registerAsReadable(ServerSocketChannel serverSocketChannel) throws Exception {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void readAndAnswer(SelectionKey selectionKey) throws Exception {
        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            clientSocketChannel.read(buffer);
            String requestString = new String(buffer.array()).trim();
            if (!requestString.equals("")) {
                if (requestString.equals("exit")) {
                    stop();
                    return;
                }
                answer(clientSocketChannel, requestString);
            }
            buffer.clear();
        } catch (Exception exception) {
            logger.error("1");
            logger.error(exception);
        }
    }

    private void answer(SocketChannel clientSocketChannel, String requestString) {
        executorService.execute(() -> {
            try {
                String responseString = dataProcessor.processData(requestString);
                clientSocketChannel.write(ByteBuffer.wrap(responseString.getBytes()));
                clientSocketChannel.close();
                logger.debug("sent response: " + responseString);
            } catch (Exception exception) {
                logger.error(exception);
            }
        });
    }
}
