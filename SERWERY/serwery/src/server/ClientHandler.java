package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import static server.ChatServer.broadcast;

public class ClientHandler implements Runnable{
    private Socket socket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;

        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        out.println(msg);
    }

    @Override
    public void run() {
        try {
            out.println("Wprowadź swój login:");
            String desired = in.readLine();
            synchronized (clients) {
                for (ClientHandler ch : clients) {
                    if (desired.equalsIgnoreCase(ch.username)) {
                        out.println("LOGIN_ERROR: login zajęty");
                        closeConnection();
                        return;
                    }
                }
            }
            username = desired;
            broadcast(username + " dołączył do czatu");

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("/online")) {
                    StringBuilder sb = new StringBuilder("Online:");
                    synchronized (clients) {
                        for (ClientHandler ch : clients) {
                            sb.append(" ").append(ch.username).append(",");
                        }
                    }
                    out.println(sb.substring(0, sb.length() - 1));
                }
                else if (line.startsWith("/w ")) {
                    String[] parts = line.split("\\s+", 3);
                    if (parts.length < 3) {
                        out.println("BŁĄD: format /w użytkownik wiadomość");
                        continue;
                    }
                    String rec = parts[1], msg = parts[2];
                    boolean found = false;
                    synchronized (clients) {
                        for (ClientHandler ch : clients) {
                            if (ch.username.equalsIgnoreCase(rec)) {
                                ch.send("(prywatnie) " + username + ": " + msg);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        out.println("Użytkownik '" + rec + "' nie jest online.");
                    }
                }
                else {
                    broadcast(username + ": " + line);
                }
            }
        } catch (IOException e) {
        } finally {
            closeConnection();
            broadcast(username + " opuścił czat");
        }
    }

    private void closeConnection() {
        try {
            clients.remove(this);
            if (in  != null) in .close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
