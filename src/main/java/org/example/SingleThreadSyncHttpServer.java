package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SingleThreadSyncHttpServer {

    private final ServerSocket serverSocket;

    public SingleThreadSyncHttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void run() {
        System.out.println("Server Starting");

        while (true) {
            try (Socket socket = serverSocket.accept()) {
                process(socket);
            } catch (IOException e) {
                handleError("Error accepting connection", e);
            }
        }
    }

    private void process(Socket socket) {
        if (socket == null) {
            return;
        }


        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String requestMessage = readRequest(in);
            System.out.println("Received:\n" + requestMessage);
            String responseMessage = createResponse("Hello, world!");
            sendMessage(out, responseMessage);
            System.out.println("Sent response");

        } catch (IOException e) {
            handleError("Error processing request", e);
        } finally {
            closeIgnoringException(socket);
        }
    }

    private String readRequest(BufferedReader in) throws IOException {
        StringBuilder request = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            request.append(line).append("\n");
        }
        return request.toString();
    }

    private String createResponse(String body) {

        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" + body;
    }

    private static void sendMessage(BufferedWriter out, String message) throws IOException {
        out.write(message);
        out.flush();
    }

    private void closeIgnoringException(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            handleError("Error closing socket", e);
        }
    }

    private void handleError(String message, Exception e) {
        e.printStackTrace();
        System.err.println(message + ": " + e.getMessage());
    }

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
