package com.andersenlab.client_server.tcp;

import com.andersenlab.client_server.DataProcessor;
import com.andersenlab.client_server.ServerInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
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
    private TreeMap<Long, SocketChannel> socketChannelQueue = new TreeMap<>();
    private TreeMap<Long, String> requestByQueueKey = new TreeMap<>();

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
                proceedAnswerQueue();
            }
        }
        logger.debug("Selector was closed");
    }

    private void registerAsReadable(ServerSocketChannel serverSocketChannel) throws Exception {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ, new RequestStringStack());
    }

    private void readAndAnswer(SelectionKey selectionKey) throws Exception {
        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
        RequestStringStack requestStack = (RequestStringStack) selectionKey.attachment();
        ByteBuffer buffer = ByteBuffer.allocate(byteBufferSize);
        try {
            int lengthOfRead = clientSocketChannel.read(buffer);
            while (!requestStack.isFinishedLine() && lengthOfRead > 0) {
                requestStack.pushChunk(Arrays.copyOfRange(buffer.array(), 0, lengthOfRead));
                buffer.clear();
                lengthOfRead = clientSocketChannel.read(buffer);
            }
            if (requestStack.isFinishedLine()) {
                handleCollectedRequest(clientSocketChannel, requestStack);
            }
        } catch (Exception exception) {
            logger.error(exception);
        }
    }

    private void handleCollectedRequest(SocketChannel clientSocketChannel, RequestStringStack requestStack) {
        if (requestStack.getString().equals("exit")) {
            stop();
        } else {
            addToQueue(clientSocketChannel, requestStack.getString());
        }
    }

    private void addToQueue(SocketChannel clientSocketChannel, String requestString) {
        Long queueKey = System.currentTimeMillis();
        socketChannelQueue.put(queueKey, clientSocketChannel);
        requestByQueueKey.put(queueKey, requestString);
    }

    private void proceedAnswerQueue() {
        for (Map.Entry socketChannelEntry: socketChannelQueue.entrySet()) {
            Long entryKey = (Long) socketChannelEntry.getKey();
            String requestString = requestByQueueKey.get(entryKey);
            SocketChannel clientSocketChannel = (SocketChannel) socketChannelEntry.getValue();
            answer(clientSocketChannel, requestString);
        }
    }

    private void answer(SocketChannel clientSocketChannel, String requestString) {
        try {
            String responseString = dataProcessor.processData(requestString) + endOfLine;
            if (clientSocketChannel.isOpen()) {
                clientSocketChannel.write(ByteBuffer.wrap(responseString.getBytes()));
                logger.debug("sent response: " + responseString);
            }
        } catch (Exception exception) {
            logger.error(exception);
        } finally {
            safelyClose(clientSocketChannel);
        }
    }

    private void safelyClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception exeption) {
            logger.error(exeption);
        }
    }

    private static class RequestStringStack {
        private String string = "";

        private void pushChunkBeforeEndOfLine(String chunk) {
            if (chunk.contains(endOfLine)) {
                int endOfLineStartPosition = chunk.indexOf(endOfLine);
                chunk = chunk.substring(0, endOfLineStartPosition + endOfLine.length());
            }
            string += chunk;
        }

        private void pushChunk(byte[] bytes) {
            pushChunkBeforeEndOfLine(new String(trimBytes(bytes)));
        }

        private String getString() {
            return string.trim();
        }

        private boolean isFinishedLine() {
            return string.endsWith(endOfLine);
        }

        private static byte[] trimBytes(byte[] bytes)
        {
            int i = bytes.length - 1;
            while (i >= 0 && bytes[i] == 0) {
                --i;
            }

            return Arrays.copyOf(bytes, i + 1);
        }
    }
}
