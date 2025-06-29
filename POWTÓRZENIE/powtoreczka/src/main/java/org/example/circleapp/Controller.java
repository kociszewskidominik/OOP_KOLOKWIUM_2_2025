package org.example.circleapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.example.circleapp.client.ServerThread;

public class Controller {
    @FXML private Canvas    canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider      radiusSlider;

    private final ServerThread serverThread;
    private       GraphicsContext graphics;

    public Controller(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    @FXML
    public void initialize() {
        graphics = canvas.getGraphicsContext2D();

        serverThread.setDotConsumer(dot -> {
            Platform.runLater(() -> {
                graphics.setFill(Color.web(dot.color()));
                graphics.fillOval(
                        dot.x() - dot.radius(),
                        dot.y() - dot.radius(),
                        dot.radius() * 2,
                        dot.radius() * 2
                );
            });
        });
    }

    @FXML
    private void onMouseClicked(MouseEvent event) {
        double x = event.getX(), y = event.getY(), r = radiusSlider.getValue();
        Color c = colorPicker.getValue();
        String colorStr = String.format(
                "#%02X%02X%02X",
                (int)(c.getRed()   * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue()  * 255)
        );

        graphics.setFill(c);
        graphics.fillOval(x - r, y - r, r * 2, r * 2);

        serverThread.send(colorStr, r, x, y);
    }

    @FXML private void onStartServerClicked() {}
    @FXML private void onConnectClicked() {}
}
