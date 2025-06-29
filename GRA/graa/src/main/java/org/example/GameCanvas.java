package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.Ball;
import org.example.Brick;
import org.example.GraphicsItem;
import org.example.Paddle;

import java.util.ArrayList;
import java.util.Iterator;

public class GameCanvas extends Canvas {
    private final GraphicsContext graphicsContext;
    private final Paddle paddle;
    private final Ball ball;
    private boolean gameStarted = false;
    private AnimationTimer timer;
    private long lastFrameTime = 0;

    private final ArrayList<Brick> bricks = new ArrayList<>();

    private int score = 0;
    private int bounceCounter = 0;

    private static final int maxScore= 5;
    private static final int maxBounces= 15;

    public GameCanvas(double width, double height) {
        super(width, height);
        this.graphicsContext = getGraphicsContext2D();
        GraphicsItem.setCanvasSize(width, height);

        this.paddle = new Paddle();
        this.ball = new Ball();
        this.ball.setVelocity(0.5);

        setupAnimationLoop();

        setOnMouseMoved(e -> {
            paddle.moveTo(e.getX());
            draw();
        });
        setOnMouseClicked(e -> gameStarted = true);

        loadLevel();
    }

    public void draw() {
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, getWidth(), getHeight());

        paddle.draw(graphicsContext);
        ball.draw(graphicsContext);

        for (Brick brick : bricks) {
            brick.draw(graphicsContext);
        }

        drawScore(graphicsContext);
    }

    private void setupAnimationLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }
                double elapsedSeconds = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                if (gameStarted) {
                    ball.updatePosition(elapsedSeconds);
                    if (ball.getBottom() >= 1) {
                        endGame(false);
                        return;
                    }
                } else {
                    Point2D paddleCenter = new Point2D(
                            paddle.getX() + paddle.getWidth() / 2,
                            paddle.getY() - ball.getHeight()
                    );
                    ball.setPosition(paddleCenter);
                }

                if (shouldBallBounceHorizontally()) {
                    ball.bounceHorizontally();
                }
                if (shouldBallBounceVertically()) {
                    ball.bounceVertically();
                }
                if (shouldBallBounceFromPaddle()) {
                    bounceCounter++;
                    if (bounceCounter > maxBounces) {
                        endGame(false);
                        return;
                    }
                    double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
                    double ballCenter = ball.getLeft() + ball.getWidth() / 2;
                    double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);
                    ball.bounceFromPaddle(offset);
                }

                Iterator<Brick> iterator = bricks.iterator();
                while (iterator.hasNext()) {
                    Brick brick = iterator.next();
                    Brick.CrushType result = brick.checkCollision(
                            ball.getLeft(), ball.getRight(), ball.getTop(), ball.getBottom()
                    );
                    if (result != Brick.CrushType.NoCrush) {
                        iterator.remove();
                        score++;
                        if (score >= maxScore) {
                            endGame(true);
                            return;
                        }
                        if (result == Brick.CrushType.HorizontalCrush) {
                            ball.bounceHorizontally();
                        } else {
                            ball.bounceVertically();
                        }
                        break;
                    }
                }

                draw();
            }
        };
        timer.start();
    }

    private void endGame(boolean won) {
        timer.stop();
        String msg;

        if (won) {
            msg = "WYGRANA";
        } else {
            msg = "PRZEGRANA";
        }

        String msg2 = msg + "\nWynik: " + score;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg2, ButtonType.OK);
            alert.setTitle("KONIEC GRY");
            alert.setHeaderText(null);
            alert.showAndWait();

            Platform.exit();
        });
    }

    private void drawScore(GraphicsContext gc){
        gc.setFont(Font.font(18));
        gc.setFill(Color.HOTPINK);

        String scoreText = "Punkty: " + score;
        String bounceText = "Odbicia: " + bounceCounter + " / " + maxBounces;

        gc.fillText(scoreText, 10, 20);
        gc.fillText(bounceText, 10, 40);
    }

    private boolean shouldBallBounceHorizontally() {
        return ball.getX() <= 0 || (ball.getX() + ball.getWidth()) >= 1;
    }

    private boolean shouldBallBounceVertically() {
        return ball.getY() <= 0;
    }

    private boolean shouldBallBounceFromPaddle() {
        return ball.getY() + ball.getHeight() >= paddle.getY() &&
                ball.getY() <= paddle.getY() + paddle.getHeight() &&
                ball.getX() + ball.getWidth() >= paddle.getX() &&
                ball.getX() <= paddle.getX() + paddle.getWidth();
    }

    private void loadLevel() {
        Brick.setGridSize(20, 10);
        bricks.clear();

        Color[] rowColors = {
                Color.RED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.BLUE, Color.VIOLET
        };

        for (int row = 2; row <= 7; row++) {
            Color color = rowColors[row - 2];
            for (int col = 0; col < 10; col++) {
                bricks.add(new Brick(col, row, color));
            }
        }
    }
}
