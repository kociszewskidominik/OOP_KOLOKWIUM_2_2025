package org.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Paddle extends GraphicsItem {

    private final Color color = Color.LIGHTBLUE;

    public Paddle() {
        this.width = 0.2;
        this.height = 0.03;
        this.x = 0.4;
        this.y = 1.0 - this.height - 0.02;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(
                x * canvasWidth,
                y * canvasHeight,
                width * canvasWidth,
                height * canvasHeight
        );
    }

    public void moveTo(double mouseX) {
        double relativeX = mouseX / canvasWidth;
        this.x = relativeX - this.width / 2;
        if (x < 0) x = 0;
        if (x + width > 1.0) x = 1.0 - width;
    }
}
