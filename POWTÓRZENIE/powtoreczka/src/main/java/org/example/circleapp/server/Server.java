package org.example.circleapp.server;

import org.example.circleapp.shared.Dot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private final List<ClientThread> clients = new ArrayList<>();
    private Connection connection;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        connect();
        try {
            serverSocket = new ServerSocket(port);

            while(true) {
                Socket clientSocket =  serverSocket.accept();

                ClientThread clientThread = new ClientThread(this, clientSocket);
                clients.add(clientThread);

                sendAllDotsTo(clientThread);

                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        System.out.println("Server.broadcast: " + message);
        Dot dot = Dot.fromMessage(message);
        saveDot(dot);

        for (ClientThread client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientThread clientThread) {
        clients.remove(clientThread);
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:circles.db");

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS dot (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "x INTEGER NOT NULL, " + "y INTEGER NOT NULL, " +
                                "color TEXT NOT NULL, " +
                                "radius INTEGER NOT NULL)"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveDot(Dot dot) {
        String sql = "INSERT INTO dot(x, y, color, radius) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, (int) dot.x());
            pstmt.setInt(2, (int) dot.y());
            pstmt.setString(3, dot.color());
            pstmt.setInt(4, (int) dot.radius());
            pstmt.executeUpdate();
            System.out.println("Saved to DB: " + dot);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Dot> getSavedDots() {
        List<Dot> dots = new ArrayList<>();
        String sql = "SELECT x, y, color, radius FROM dot";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double x = rs.getInt("x");
                double y = rs.getInt("y");
                double radius = rs.getInt("radius");
                String color = rs.getString("color");
                dots.add(new Dot(x, y, radius, color));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dots;
    }

    public void sendAllDotsTo(ClientThread client) {
        List<Dot> allDots = getSavedDots();
        for (Dot dot : allDots) {
            client.sendMessage(dot.toMessage());
        }
    }
}
