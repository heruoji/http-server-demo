package org.example;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            SingleThreadSyncHttpServer server = new SingleThreadSyncHttpServer(8080);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}