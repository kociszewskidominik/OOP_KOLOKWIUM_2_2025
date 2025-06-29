// File: ClientReceiver.java
package org.example.client;

import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientReceiver implements MessageListener {
    private final TextArea textArea;
    private final ObservableList<String> users;

    public ClientReceiver(TextArea textArea, ObservableList<String> users) {
        this.textArea = textArea;
        this.users = users;
    }

    private void onBroadcast(String msg) {
        textArea.appendText(msg + "\n");
    }

    private void onWhisper(String msg) {
        textArea.appendText("(prywatnie) " + msg + "\n");
    }

    private void onLoginBroadcast(String user) {
        users.add(user);
    }

    private void onLogoutBroadcast(String user) {
        users.remove(user);
    }

    private void onOnline(String msg) {
        String[] parts = msg.substring("Online:".length()).split(",");
        users.setAll(Arrays.stream(parts)
                .map(String::trim)
                .collect(Collectors.toList()));
    }

    private void onFile(String msg) {
        textArea.appendText("[Plik] " + msg + "\n");
    }

    @Override
    public void onMessage(String msg) {
        if (msg.startsWith("Online:")) {
            onOnline(msg);
        } else if (msg.startsWith("[LOGIN]")) {
            onLoginBroadcast(msg.substring("[LOGIN]".length()).trim());
        } else if (msg.startsWith("[LOGOUT]")) {
            onLogoutBroadcast(msg.substring("[LOGOUT]".length()).trim());
        } else if (msg.startsWith("/w ")) {
            onWhisper(msg);
        } else if (msg.startsWith("[FILE]")) {
            onFile(msg.substring("[FILE]".length()).trim());
        } else {
            onBroadcast(msg);
        }
    }
}
