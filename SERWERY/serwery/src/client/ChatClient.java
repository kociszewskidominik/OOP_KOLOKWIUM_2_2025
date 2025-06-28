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
