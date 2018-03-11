package com.andersenlab.client_server.tcp;

import java.nio.channels.SocketChannel;
import java.util.Arrays;

class RequestHolder {
    private static final String endOfLine = "\n";

    private SocketChannel socketChannel;
    private RequestStringStack requestStringStack;

    RequestHolder(SocketChannel socketChannel, RequestStringStack requestStringStack) {
        this.socketChannel = socketChannel;
        this.requestStringStack = requestStringStack;
    }

    SocketChannel getSocketChannel() {
        return socketChannel;
    }

    RequestStringStack getRequestStringStack() {
        return requestStringStack;
    }

    static class RequestStringStack {
        private String string = "";

        private void pushChunkBeforeEndOfLine(String chunk) {
            if (chunk.contains(endOfLine)) {
                int endOfLineStartPosition = chunk.indexOf(endOfLine);
                chunk = chunk.substring(0, endOfLineStartPosition + endOfLine.length());
            }
            string += chunk;
        }

        void pushChunk(byte[] bytes) {
            pushChunkBeforeEndOfLine(new String(trimBytes(bytes)));
        }

        String getString() {
            return string.trim();
        }

        boolean isFinishedLine() {
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
