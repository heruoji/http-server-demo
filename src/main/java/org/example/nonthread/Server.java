package org.example.nonthread;

import org.example.common.MessageUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server implements Runnable {
    ServerSocket serverSocket;
    volatile boolean keepProcessing = true;

    public Server(int port, int millisecondsTimeout) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(0);
    }

    public void run() {
        System.out.println("Server Starting");

        while (keepProcessing) {
            try {
                System.out.println("accepting client");
                Socket socket = null;
                socket = serverSocket.accept();
                System.out.println("got client");
                process(socket);
            } catch (Exception e) {
                handle(e);
            }
        }
    }

    private void handle(Exception e) {
        if (!(e instanceof SocketException)) {
            e.printStackTrace();
        }
    }

    public void stopProcessing() {
        keepProcessing = false;
        closeIgnoringException(serverSocket);
    }

    void process(Socket socket) throws SocketException {
        if (socket == null) {
            return;
        }

        try {
            System.out.println("Server: getting message");
            String message = MessageUtils.getMessage(socket);
            System.out.printf("Server: got message: %s\n", message);
            Thread.sleep(1000);
            System.out.printf("Server: sending reply: %s\n", message);
            MessageUtils.sendMessage(socket, "Processed: " + message);
            System.out.println("Server: sent");
            closeIgnoringException(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeIgnoringException(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }

    private void closeIgnoringException(ServerSocket serverSocket) {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(8009, 2000);
            Thread serverThread = new Thread(server);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
