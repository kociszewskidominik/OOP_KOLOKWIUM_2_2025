# GRA
### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>Kolo2PowtJavaFx</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Kolo2PowtJavaFx</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.2</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>17.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>17.0.6</version>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>24</source>
                    <target>24</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <executions>
                    <execution>
                        <id>default-cli</id>
                        <configuration>
                            <mainClass>org.example.Main</mainClass>
                            <launcher>app</launcher>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
### Klasa Ball
```java
package org.example;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ball extends GraphicsItem {

    private Point2D moveVector;
    private double velocity;

    public Ball() {
        this.width = 0.03;
        this.height = 0.03;

        // Początkowy kierunek: 45 stopni w prawo i w górę
        this.moveVector = new Point2D(1, -1).normalize();
        this.velocity = 0.005; // jednostki kanwy na klatkę
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void setPosition(Point2D point) {
        this.x = point.getX() - this.width / 2;
        this.y = point.getY() - this.height / 2;
    }

    public void updatePosition(double elapsedSeconds) {
        this.x += this.moveVector.getX() * this.velocity * elapsedSeconds;
        this.y += this.moveVector.getY() * this.velocity * elapsedSeconds;
    }

    // Zad. 7
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillOval(
                x * canvasWidth,
                y * canvasHeight,
                width * canvasWidth,
                height * canvasHeight
        );
    }

    // Zad.5

    public void bounceHorizontally() {
        moveVector = new Point2D(-moveVector.getX(), moveVector.getY());
    }

    public void bounceVertically() {
        moveVector = new Point2D(moveVector.getX(), -moveVector.getY());
    }

    // Zad. 7

    public double getTop() {
        return y;
    }

    public double getBottom() {
        return y + height;
    }

    public double getLeft() {
        return x;
    }

    public double getRight() {
        return x + width;
    }

    // Zad.8

    public void bounceFromPaddle(double offset) {
        // Zakładamy, że offset jest z przedziału [-1, 1]
        // -1 = lewa krawędź paletki, 0 = środek, 1 = prawa krawędź

        // Kąt odbicia będzie się zmieniał między 45° w lewo (-45°) a 45° w prawo (45°)
        double maxAngle = Math.toRadians(60); // większy zakres = ostrzejsze kąty
        double angle = offset * maxAngle;

        // Nowy znormalizowany wektor ruchu
        double dx = Math.sin(angle);
        double dy = -Math.cos(angle); // ujemny, bo piłka odbija się w górę

        moveVector = new Point2D(dx, dy).normalize();
    }
}
```
### Klasa Brick
```java
package org.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Brick extends GraphicsItem {
    private static int gridRows;
    private static int gridCols;

    private final Color color;

    // Zad. 6

    public enum CrushType {
        NoCrush,
        HorizontalCrush,
        VerticalCrush
    }

    public static void setGridSize(int rows, int cols) {
        gridRows = rows;
        gridCols = cols;
    }

    public Brick(int gridX, int gridY, Color color) {
        this.color = color;

        this.x = (double) gridX / gridCols;
        this.y = (double) gridY / gridRows;
        this.width = 1.0 / gridCols;
        this.height = 1.0 / gridRows;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double realX = x * canvasWidth;
        double realY = y * canvasHeight;
        double realW = width * canvasWidth;
        double realH = height * canvasHeight;

        // Główna część
        gc.setFill(color);
        gc.fillRect(realX, realY, realW, realH);

        // Cień dla "trójwymiarowości"
        gc.setStroke(Color.GRAY);
        gc.strokeLine(realX, realY, realX + realW, realY); // górna krawędź
        gc.strokeLine(realX, realY, realX, realY + realH); // lewa krawędź
    }

    // Zad. 6

    public CrushType checkCollision(double ballLeft, double ballRight, double ballTop, double ballBottom) {
        double brickLeft = this.x;
        double brickRight = this.x + this.width;
        double brickTop = this.y;
        double brickBottom = this.y + this.height;

        boolean intersects = ballRight > brickLeft && ballLeft < brickRight &&
                ballBottom > brickTop && ballTop < brickBottom;

        if (!intersects) {
            return CrushType.NoCrush;
        }

        double ballCenterX = (ballLeft + ballRight) / 2;
        double ballCenterY = (ballTop + ballBottom) / 2;

        double brickCenterX = (brickLeft + brickRight) / 2;
        double brickCenterY = (brickTop + brickBottom) / 2;

        double dx = Math.abs(ballCenterX - brickCenterX);
        double dy = Math.abs(ballCenterY - brickCenterY);

        if (dx > dy) {
            return CrushType.HorizontalCrush;
        } else {
            return CrushType.VerticalCrush;
        }
    }
}
```
### Klasa Paddle
```java
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
```
### Klasa GraphicsItem
```java
package org.example;

import javafx.scene.canvas.GraphicsContext;

public abstract class GraphicsItem {

    // Wspólne wymiary kanwy (dla przeliczeń względnych)
    protected static double canvasWidth;
    protected static double canvasHeight;

    public static void setCanvasSize(double width, double height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    // Pozycja i rozmiar w przestrzeni względnej (0.0 - 1.0)
    protected double x;
    protected double y;
    protected double width;
    protected double height;

    // Gettery i settery
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    // Abstrakcyjna metoda rysująca
    public abstract void draw(GraphicsContext gc);
}
```
### Klasa GameCanvas
```java
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
```
### Klasa Main
```java
package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameCanvas gameCanvas = new GameCanvas(512, 512);
        gameCanvas.draw();

        StackPane root = new StackPane(gameCanvas);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Gra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```
