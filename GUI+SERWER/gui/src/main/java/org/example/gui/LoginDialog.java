package org.example.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginDialog extends Stage {
    private TextField loginField;
    private String login;

    public String showAndWaitLogin() {
        loginField = new TextField();
        loginField.setPrefWidth(180);

        Button button = new Button("Zaloguj...");
        button.setPrefWidth(180);
        button.setOnAction(e -> {
            login = loginField.getText();
            this.close();
        });

        GridPane pane = new GridPane();
        pane.add(loginField, 0, 0);
        pane.add(button, 0, 1);

        GridPane.setHgrow(loginField, Priority.ALWAYS);
        GridPane.setHgrow(button, Priority.ALWAYS);

        Scene scene = new Scene(pane, 200, 150);
        initModality(Modality.APPLICATION_MODAL);
        setScene(scene);
        showAndWait();
        return login;
    }
}
