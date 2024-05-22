package org.example.nonthread;


import org.example.common.MessageUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

public class ClientTest {
    private static final int PORT = 8009;
    private static final int TIMEOUT = 2000;

    Server server;
    Thread serverThread;

    @BeforeEach
    public void createServer() throws IOException {
        try {
            server = new Server(PORT, TIMEOUT);
            serverThread = new Thread(server);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @AfterEach
    public void shutdownServer() throws InterruptedException {
        if (server != null) {
            server.stopProcessing();
            serverThread.join();
        }
    }

    class TrivialClient implements Runnable {
        int clientNumber;

        public TrivialClient(int clientNumber) {
            this.clientNumber = clientNumber;
        }

        public void run() {
            try {
                connectSendReceiver(clientNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void shouldRunInUnder10Seconds() throws Exception {
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(new TrivialClient(i));
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    private void connectSendReceiver(int i) throws IOException {

        System.out.printf("Client %2d: connecting\n", i);
        Socket socket = new Socket("localhost", PORT);
        System.out.printf("Client %2d: sending message\n", i);
        MessageUtils.sendMessage(socket, Integer.toString(i));
        System.out.printf("Client %2d: getting reply\n", i);
        MessageUtils.getMessage(socket);
        System.out.printf("Client %2d: finished\n", i);
        socket.close();
    }
}
