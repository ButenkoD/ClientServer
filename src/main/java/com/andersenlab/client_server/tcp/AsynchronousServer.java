package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class AsynchronousServer implements ServerInterface {
    private static final int port = 22211;
    private static final int byteBufferSize = 4;
    private static final String endOfLine = "\n";
    private static final Logger logger = LogManager.getLogger(AsynchronousServer.class);
    private final DataProcessor dataProcessor = new DataProcessor();
    private Selector selector;
    private boolean isListening;
    private TreeMap<Long, RequestHolder> requestByQueueKey = new TreeMap<>();

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
            try {
                if (selector != null) {
                    selector.close();
                }
            } catch (Exception exception) {
                logger.error("Can't close selector");
            }
        }
    }

    private void initServerSocketChannel(ServerSocketChannel serverSocketChannel) throws Exception {
        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void performListening(ServerSocketChannel serverSocketChannel) throws Exception {
        long timeout = 1000;
        while (selector.isOpen()) {
            if (selector.select(timeout) > 0) {
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
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<Long, RequestHolder>> iterator = requestByQueueKey.entrySet().iterator();
            for (; iterator.hasNext();) {
                Map.Entry<Long, RequestHolder> requestEntry = iterator.next();
                Long entryKey = requestEntry.getKey();
                if (now >= entryKey + 1000) {
                    answer(requestEntry.getValue());
                    iterator.remove();
                } else {
                    timeout = entryKey + 1000 - now;
                    break;
                }
            }
        }
        logger.debug("Selector was closed");
    }

    private void registerAsReadable(ServerSocketChannel serverSocketChannel) throws Exception {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ, new RequestHolder.RequestStringStack());
    }

    private void readAndAnswer(SelectionKey selectionKey) throws Exception {
        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
        RequestHolder.RequestStringStack requestStack = (RequestHolder.RequestStringStack) selectionKey.attachment();
        ByteBuffer buffer = ByteBuffer.allocate(byteBufferSize);
        try {
            int lengthOfRead = clientSocketChannel.read(buffer);
            while (!requestStack.isFinishedLine() && lengthOfRead > 0) {
                requestStack.pushChunk(Arrays.copyOfRange(buffer.array(), 0, lengthOfRead));
                buffer.clear();
                lengthOfRead = clientSocketChannel.read(buffer);
            }
            if (requestStack.isFinishedLine()) {
                handleCollectedRequest(new RequestHolder(clientSocketChannel, requestStack));
            }
        } catch (Exception exception) {
            logger.error(exception);
        }
    }

    private void handleCollectedRequest(RequestHolder requestHolder) {
        if (requestHolder.getRequestStringStack().getString().equals("exit")) {
            stop();
        } else {
            addToQueue(requestHolder);
        }
    }

    private void addToQueue(RequestHolder requestHolder) {
        Long queueKey = System.currentTimeMillis();
        requestByQueueKey.put(queueKey, requestHolder);
    }

    private void answer(RequestHolder requestHolder) {
        try (
            SocketChannel socketChannel = requestHolder.getSocketChannel()
        ) {
            String responseString = dataProcessor.processData(requestHolder.getRequestStringStack().getString()) + endOfLine;
            if (socketChannel.isOpen()) {
                socketChannel.write(ByteBuffer.wrap(responseString.getBytes()));
                logger.debug("sent response: " + responseString);
            }
        } catch (Exception exception) {
            logger.error(exception);
        }
    }
}
