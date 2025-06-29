package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    public ChatClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(() -> {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    for (MessageListener listener : listeners) {
                        listener.onMessage(line);
                    }
                }
            } catch (IOException e) {
            }
        }).start();
    }

    public void send(String msg) {
        out.println(msg);
    }

    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (in != null) in.close();
        if (out != null) out.close();
    }
}
