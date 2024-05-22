package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SingleThreadAsyncHttpServer {

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    public SingleThreadAsyncHttpServer(int port) throws IOException {
        this.selector = Selector.open();
        this.serverSocketChannel = ServerSocketChannel.open();
        initializeServer(port);
    }

    private void initializeServer(int port) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        System.out.println("Server starting");

        try {
            while (true) {
                selector.select();
                handleKeys();
            }
        } catch (IOException e) {
            handleError(e);
        }
    }

    private void handleKeys() throws IOException {
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();
            if (!key.isValid()) {
                continue;
            }
            if (key.isAcceptable()) {
                accept(key);
            } else if (key.isReadable()) {
                read(key);
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    private void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        if (!socketChannel.isOpen()) {
            return;
        }
        try {
            String request = getRequest(socketChannel);
            System.out.println("Received: " + request);
            sendResponse(socketChannel);
        } catch (IOException e) {
            handleError(e);
            closeSocketChannel(socketChannel);
        } finally {
        }
    }

    private String getRequest(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        StringBuilder requestBuilder = new StringBuilder();
        int bytesRead;
        Charset charset = StandardCharsets.UTF_8;

        while (true) {
            buffer.clear();
            bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                closeSocketChannel(socketChannel);
                throw new IOException("Connection closed by client");
            }
            if (bytesRead > 0) {
                buffer.flip();
                requestBuilder.append(charset.decode(buffer));
            }
            if (bytesRead < buffer.capacity()) {
                break;
            }
        }
        return requestBuilder.toString();
    }

    private void sendResponse(SocketChannel socketChannel) throws IOException {
        if (!socketChannel.isOpen()) {
            return;
        }
        String httpResponse =
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: 13\r\n" +
                        "\r\n" +
                "Hello, World!";
        ByteBuffer responseBuffer = ByteBuffer.wrap(httpResponse.getBytes());
        while (responseBuffer.hasRemaining()) {
            socketChannel.write(responseBuffer);
        }
        System.out.println("Sent response");
    }

    private void closeSocketChannel(SocketChannel socketChannel) {
        try {
            if (socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException ex) {
            handleError(ex);
        }
    }

    private void handleError(Exception e) {
        e.printStackTrace();
        System.err.println("Error in server: " + e.getMessage());
    }

    public static void main(String[] args) {
        try {
            SingleThreadAsyncHttpServer server = new SingleThreadAsyncHttpServer(8080);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
