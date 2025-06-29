package org.example.circleapp.client;

import org.example.circleapp.shared.Dot;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerThread implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<Dot> dotConsumer;

    public ServerThread(String address, int port) throws IOException {
        socket = new Socket(address, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void setDotConsumer(Consumer<Dot> dotConsumer) {
        this.dotConsumer = dotConsumer;
    }

    public void send(String color, double radius, double x, double y) {
        Dot dot = new Dot(x, y, radius, color);
        String message = dot.toMessage();
        out.println(message);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                Dot dot = Dot.fromMessage(msg);
                System.out.println(dot);
                if (dotConsumer != null) {
                    dotConsumer.accept(dot);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
