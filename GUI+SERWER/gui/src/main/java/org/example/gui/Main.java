package org.example.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.example.client.ChatClient;
import org.example.client.ClientReceiver;

import java.io.IOException;

public class Main extends Application {
    private TextField textField;
    private TextArea textArea;
    private ListView<String> listView;
    private ChatClient client;
    private ObservableList<String> users;

    @Override
    public void start(Stage stage) {
        LoginDialog loginDialog = new LoginDialog();
        String login = loginDialog.showAndWaitLogin();
        if (login == null || login.isEmpty()) {
            Platform.exit();
            return;
        }

        try {
            client = new ChatClient("127.0.0.1", 2137);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            return;
        }

        users = FXCollections.observableArrayList();
        listView = new ListView<>(users);
        listView.setPrefWidth(150);

        textArea = new TextArea();
        textArea.setPrefHeight(450);
        textArea.setPrefWidth(600);
        textArea.setEditable(false);

        ClientReceiver receiver = new ClientReceiver(textArea, users);
        client.addMessageListener(receiver);

        // Po zalogowaniu automatycznie pobierz listę online
        client.send("/online");
        client.send(login);

        textField = new TextField();
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setOnAction(e -> sendMsg());

        Button button = new Button("Wyślij...");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> sendMsg());

        HBox bottom = new HBox(textField, button);
        HBox.setHgrow(textField, Priority.ALWAYS);
        HBox.setHgrow(button, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(textArea);
        root.setRight(listView);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();
    }

    private void sendMsg() {
        String text = textField.getText();
        if (text == null || text.isBlank()) return;
        client.send(text);
        textField.clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
