package org.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                ch.send(message);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 2137;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer działa na porcie " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, clients);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}