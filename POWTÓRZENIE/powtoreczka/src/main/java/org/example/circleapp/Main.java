package org.example.circleapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.circleapp.client.ServerThread;
import org.example.circleapp.server.Server;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        int port = 5000;
        String address = "localhost";

        Server server = new Server(port);
        Thread srv = new Thread(server::start);
        srv.setDaemon(true);
        srv.start();

        ServerThread client = null;
        for (int i = 0; i < 10; i++) {
            try {
                client = new ServerThread(address, port);
                break;
            } catch (Exception e) {
                Thread.sleep(100);
            }
        }
        if (client == null) {
            throw new RuntimeException("Nie udało się połączyć z serwerem");
        }
        Thread cli = new Thread(client);
        cli.setDaemon(true);
        cli.start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app-view.fxml"));
        ServerThread finalClient = client;
        loader.setControllerFactory(c -> new Controller(finalClient));
        Parent root = loader.load();

        stage.setTitle("Rysowanie kółek");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
