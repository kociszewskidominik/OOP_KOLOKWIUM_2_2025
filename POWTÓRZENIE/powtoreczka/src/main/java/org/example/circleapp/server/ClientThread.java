package org.example.circleapp.server;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable {
    private Server server;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                server.broadcast(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server.removeClient(this);
        }
    }
}
