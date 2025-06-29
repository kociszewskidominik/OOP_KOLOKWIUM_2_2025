# SERWERY
### Klasa ChatClient
```java
package client;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private BufferedReader console;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            console = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.print(in.readLine() + " ");
            String login = console.readLine();
            out.println(login);

            new Thread(() -> {
                try {
                    String resp;
                    while ((resp = in.readLine()) != null) {
                        System.out.println(resp);
                    }
                } catch (IOException ignored) {}
            }).start();

            String line;
            while ((line = console.readLine()) != null) {
                out.println(line);
                if (line.equalsIgnoreCase("exit")) break;
            }
            closeAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAll() throws IOException {
        if (console != null) console.close();
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null) socket.close();
    }

    public static void main(String[] args) {
        new ChatClient("127.0.0.1", 2137);
    }
}
```
Deklaracja zmiennych:
- `private Socket socket;` - przetrzymuje otwarte połączenie z serwerem.
- `private BufferedReader console;` - czyta dane wejściowe z konsoli.
- `private PrintWriter out;` - służy do wysyłania wiadomości tekstowych do serwera.
- `private BufferedReader in;` - odbieranie wiadomości od serwera.
```java
public ChatClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            console = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.print(in.readLine() + " ");
            String login = console.readLine();
            out.println(login);

            new Thread(() -> {
                try {
                    String resp;
                    while ((resp = in.readLine()) != null) {
                        System.out.println(resp);
                    }
                } catch (IOException ignored) {}
            }).start();

            String line;
            while ((line = console.readLine()) != null) {
                out.println(line);
                if (line.equalsIgnoreCase("exit")) break;
            }
            closeAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
Tworzymy nowy wątek `new Thread(() -> ...`, który stale oczekuje na wiadomość od serwera, `readLine()` zwraca odebraną wiadomość tekstową  lub `null`, każda odebrana wiadomość jest jest wyświetlana na ekranie użytkownika `System.out.println(resp);`.
```java
            String line;
            while ((line = console.readLine()) != null) {
                out.println(line);
                if (line.equalsIgnoreCase("exit")) break;
            }
            closeAll();
```
Główny wątek oczekuje, aż użytkownik wpisze coś w konsoli, każdy wiersz wypisany przez użytkownika jest wysyłany na serwer, jeśli użytkownik napisze `exit`, klient kończy działanie. `closeAll();` wywołanie metody która zamyka wszystkie otwarte strumienie oraz połączenia 
```java
    private void closeAll() throws IOException {
        if (console != null) console.close();
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null) socket.close();
    }
```
Tworzymy nową instancję klienta łączącego się z serwerem.
```java
    public static void main(String[] args) {
        new ChatClient("127.0.0.1", 2137);
    }
```
### Klasa ChatServer
```java
package server;

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
```
Lista `clients` przetrzymuje wszystkich aktywnych klientów. Metoda `broadcast(String message)` wysyła wiadomość do wszystkich aktywnych użytkowników, `synchronized` zapewnia, że tylko jeden wątek ma dostęp do listy `clients`, co chroni przed błędami wielowątkowości.
```java
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
```
Zmienna `port` określa port na którym będzie działać serwer. Nieskończona pętla która stale oczekuje na połączenia klientów, akceptuje ich połączenie i dodaje do listy.
### Klasa ClientHandler
```java
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
```
