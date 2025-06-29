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

    // Wektor ruchu piłki – kierunek, w którym leci
    private Point2D moveVector;

    // Prędkość piłki (w jednostkach kanwy na sekundę lub klatkę)
    private double velocity;

    // Konstruktor bezargumentowy – ustawia startowe wartości
    public Ball() {
        this.width = 0.03;                        // Szerokość piłki jako ułamek szerokości kanwy (3%)
        this.height = 0.03;                       // Wysokość piłki jako ułamek wysokości kanwy (3%)

        // Początkowy kierunek: w prawo i w górę pod kątem 45°
        this.moveVector = new Point2D(1, -1).normalize(); 
        // normalize() – ustawia długość wektora na 1

        this.velocity = 0.005;                    // Początkowa prędkość – jednostki kanwy na klatkę
    }

    // Ustawienie prędkości piłki
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    // Ustawienie pozycji środka piłki w punkcie 'point'
    public void setPosition(Point2D point) {
        this.x = point.getX() - this.width / 2;   // Wyśrodkowanie piłki w poziomie
        this.y = point.getY() - this.height / 2;  // Wyśrodkowanie piłki w pionie
    }

    // Aktualizacja pozycji piłki w czasie – przesunięcie zgodnie z ruchem
    public void updatePosition(double elapsedSeconds) {
        this.x += this.moveVector.getX() * this.velocity * elapsedSeconds;
        this.y += this.moveVector.getY() * this.velocity * elapsedSeconds;
    }

    // Rysowanie piłki na kanwie
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.WHITE);  // Kolor piłki – biały
        gc.fillOval(
                x * canvasWidth,          // X w pikselach
                y * canvasHeight,         // Y w pikselach
                width * canvasWidth,      // szerokość w pikselach
                height * canvasHeight     // wysokość w pikselach
        );
    }

    // Odbicie poziome – zmiana kierunku poziomego
    public void bounceHorizontally() {
        moveVector = new Point2D(-moveVector.getX(), moveVector.getY());
    }

    // Odbicie pionowe – zmiana kierunku pionowego
    public void bounceVertically() {
        moveVector = new Point2D(moveVector.getX(), -moveVector.getY());
    }

    // Pobranie górnej krawędzi piłki w układzie względnym
    public double getTop() {
        return y;
    }

    // Pobranie dolnej krawędzi piłki
    public double getBottom() {
        return y + height;
    }

    // Pobranie lewej krawędzi piłki
    public double getLeft() {
        return x;
    }

    // Pobranie prawej krawędzi piłki
    public double getRight() {
        return x + width;
    }

    // Odbicie od paletki z uwzględnieniem offsetu – kąt odbicia
    public void bounceFromPaddle(double offset) {
        // offset w [-1, 1] – gdzie piłka uderzyła w paletkę
        double maxAngle = Math.toRadians(60);     // Maksymalny kąt odbicia (±60 stopni)
        double angle = offset * maxAngle;         // Przeliczenie offsetu na kąt

        // Nowy wektor kierunku po odbiciu
        double dx = Math.sin(angle);
        double dy = -Math.cos(angle);             // Ujemny bo odbicie w górę

        moveVector = new Point2D(dx, dy).normalize(); // Ustawienie nowego kierunku
    }
}

```
### Klasa Brick
```java
package org.example;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Brick extends GraphicsItem {
    // Liczba wierszy w siatce cegieł (ustalana dla całego poziomu)
    private static int gridRows;
    // Liczba kolumn w siatce cegieł
    private static int gridCols;

    // Kolor tej cegły (ustawiany przy tworzeniu)
    private final Color color;

    // Definicja typu wyliczeniowego opisującego rezultat zderzenia
    public enum CrushType {
        NoCrush,          // Brak zderzenia
        HorizontalCrush,  // Odbicie poziome (zmiana wektora poziomo)
        VerticalCrush     // Odbicie pionowe (zmiana wektora pionowo)
    }

    // Ustawienie rozmiaru siatki cegieł dla wszystkich obiektów Brick
    public static void setGridSize(int rows, int cols) {
        gridRows = rows;
        gridCols = cols;
    }

    // Konstruktor klasy Brick
    // gridX, gridY - pozycja cegły w siatce (np. kolumna i wiersz)
    // color - kolor cegły
    public Brick(int gridX, int gridY, Color color) {
        this.color = color;

        // Obliczanie współrzędnych względnych (0.0 - 1.0)
        this.x = (double) gridX / gridCols;
        this.y = (double) gridY / gridRows;
        this.width = 1.0 / gridCols;
        this.height = 1.0 / gridRows;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Przeliczenie współrzędnych względnych na piksele kanwy
        double realX = x * canvasWidth;
        double realY = y * canvasHeight;
        double realW = width * canvasWidth;
        double realH = height * canvasHeight;

        // Wypełnianie cegły kolorem
        gc.setFill(color);
        gc.fillRect(realX, realY, realW, realH);

        // Dodanie obramowania dla efektu 3D (cienia)
        gc.setStroke(Color.GRAY);
        gc.strokeLine(realX, realY, realX + realW, realY); // górna krawędź
        gc.strokeLine(realX, realY, realX, realY + realH); // lewa krawędź
    }

    // Sprawdzenie kolizji z piłką
    // ballLeft, ballRight, ballTop, ballBottom - granice piłki
    public CrushType checkCollision(double ballLeft, double ballRight, double ballTop, double ballBottom) {
        // Obliczenie granic cegły
        double brickLeft = this.x;
        double brickRight = this.x + this.width;
        double brickTop = this.y;
        double brickBottom = this.y + this.height;

        // Warunek przecięcia prostokątów (cegła i piłka)
        boolean intersects = ballRight > brickLeft && ballLeft < brickRight &&
                             ballBottom > brickTop && ballTop < brickBottom;

        // Jeśli nie ma przecięcia, brak zderzenia
        if (!intersects) {
            return CrushType.NoCrush;
        }

        // Oblicz środek piłki
        double ballCenterX = (ballLeft + ballRight) / 2;
        double ballCenterY = (ballTop + ballBottom) / 2;

        // Oblicz środek cegły
        double brickCenterX = (brickLeft + brickRight) / 2;
        double brickCenterY = (brickTop + brickBottom) / 2;

        // Odległości w osi X i Y między środkami
        double dx = Math.abs(ballCenterX - brickCenterX);
        double dy = Math.abs(ballCenterY - brickCenterY);

        // Decyzja: czy kolizja jest bardziej pozioma, czy pionowa
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

    // Stała (final) – kolor paletki (jasnoniebieski)
    private final Color color = Color.LIGHTBLUE;

    // Konstruktor bezargumentowy – ustawia rozmiar i pozycję startową paletki
    public Paddle() {
        this.width = 0.2;                         // szerokość paletki w proporcjach (20% szerokości kanwy)
        this.height = 0.03;                       // wysokość paletki w proporcjach (3% wysokości kanwy)
        this.x = 0.4;                             // początkowa pozycja X (40% szerokości kanwy)
        this.y = 1.0 - this.height - 0.02;        // pozycja Y – blisko dolnej krawędzi kanwy (od dołu odejmujemy wysokość i odstęp 0.02)
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Ustawienie koloru wypełnienia
        gc.setFill(color);

        // Rysowanie prostokąta – przeliczenie współrzędnych względnych (0.0-1.0) na piksele
        gc.fillRect(
                x * canvasWidth,         // współrzędna X w pikselach
                y * canvasHeight,        // współrzędna Y w pikselach
                width * canvasWidth,     // szerokość w pikselach
                height * canvasHeight    // wysokość w pikselach
        );
    }

    // Metoda przesuwająca paletkę do pozycji myszki
    public void moveTo(double mouseX) {
        double relativeX = mouseX / canvasWidth;            // przeliczenie pozycji myszki (piksele) na wartość względną (0.0-1.0)
        this.x = relativeX - this.width / 2;                // ustawienie tak, żeby środek paletki był na myszce

        // Ograniczenie: paletka nie może wyjść poza lewą krawędź
        if (x < 0) x = 0;

        // Ograniczenie: paletka nie może wyjść poza prawą krawędź
        if (x + width > 1.0) x = 1.0 - width;
    }
}
```
### Klasa GraphicsItem
```java
package org.example;

import javafx.scene.canvas.GraphicsContext;

public abstract class GraphicsItem {

    // Wspólne statyczne wymiary kanwy (piksele) – dla wszystkich obiektów
    // Są ustawiane raz na początku (np. przez GameCanvas)
    protected static double canvasWidth;
    protected static double canvasHeight;

    // Statyczna metoda do ustawienia wymiarów kanwy
    // Wywołuje ją GameCanvas, żeby wszystkie obiekty znały rozmiar
    public static void setCanvasSize(double width, double height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    // Pozycja i rozmiar obiektu w przestrzeni względnej (od 0.0 do 1.0)
    // x – współrzędna lewego górnego rogu w poziomie (procent szerokości)
    protected double x;

    // y – współrzędna lewego górnego rogu w pionie (procent wysokości)
    protected double y;

    // width – szerokość obiektu w jednostkach względnych (procent szerokości)
    protected double width;

    // height – wysokość obiektu w jednostkach względnych (procent wysokości)
    protected double height;

    // Getter – pobranie wartości x
    public double getX() {
        return x;
    }

    // Setter – ustawienie wartości x
    public void setX(double x) {
        this.x = x;
    }

    // Getter – pobranie wartości y
    public double getY() {
        return y;
    }

    // Setter – ustawienie wartości y
    public void setY(double y) {
        this.y = y;
    }

    // Getter – pobranie szerokości
    public double getWidth() {
        return width;
    }

    // Setter – ustawienie szerokości
    public void setWidth(double width) {
        this.width = width;
    }

    // Getter – pobranie wysokości
    public double getHeight() {
        return height;
    }

    // Setter – ustawienie wysokości
    public void setHeight(double height) {
        this.height = height;
    }

    // Abstrakcyjna metoda draw – musi być zaimplementowana w klasach potomnych
    // Umożliwia rysowanie obiektu na podanym kontekście graficznym
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
import java.util.ArrayList;
import java.util.Iterator;

public class GameCanvas extends Canvas {
    // Kontekst graficzny do rysowania na kanwie
    private final GraphicsContext graphicsContext;

    // Paletka gracza
    private final Paddle paddle;

    // Piłka gry
    private final Ball ball;

    // Flaga: czy gra jest aktywna
    private boolean gameStarted = false;

    // Timer do animacji (pętla gry)
    private AnimationTimer timer;

    // Czas poprzedniej klatki – do obliczania delta time
    private long lastFrameTime = 0;

    // Lista cegieł na planszy
    private final ArrayList<Brick> bricks = new ArrayList<>();

    // Wynik gracza (liczba zbitych cegieł)
    private int score = 0;

    // Licznik odbić od paletki
    private int bounceCounter = 0;

    // Konfiguracja końca gry
    private static final int maxScore = 5;         // maksymalny wynik, żeby wygrać
    private static final int maxBounces = 15;      // limit odbić zanim przegrasz

    // Konstruktor – tworzy kanwę gry o zadanych wymiarach
    public GameCanvas(double width, double height) {
        super(width, height);                                  // ustawia rozmiar kanwy
        this.graphicsContext = getGraphicsContext2D();         // pobiera kontekst graficzny
        GraphicsItem.setCanvasSize(width, height);             // ustawia wymiary kanwy dla wszystkich obiektów

        this.paddle = new Paddle();                            // tworzy paletkę
        this.ball = new Ball();                                // tworzy piłkę
        this.ball.setVelocity(0.5);                            // ustawia prędkość początkową

        setupAnimationLoop();                                  // ustawia pętlę animacji

        // Obsługa ruchu myszki – przesuwanie paletki
        setOnMouseMoved(e -> {
            paddle.moveTo(e.getX());
            draw();
        });

        // Kliknięcie myszką – start gry
        setOnMouseClicked(e -> gameStarted = true);

        loadLevel();                                            // załaduj cegły na planszy
    }

    // Metoda do rysowania całej sceny
    public void draw() {
        graphicsContext.setFill(Color.BLACK);                   // tło czarne
        graphicsContext.fillRect(0, 0, getWidth(), getHeight()); // wypełnij tło

        paddle.draw(graphicsContext);                           // narysuj paletkę
        ball.draw(graphicsContext);                             // narysuj piłkę

        for (Brick brick : bricks) {                            // narysuj wszystkie cegły
            brick.draw(graphicsContext);
        }

        drawScore(graphicsContext);                             // narysuj wynik i liczbę odbić
    }

    // Konfiguracja głównej pętli animacji gry
    private void setupAnimationLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) {                       // pierwsza klatka – inicjalizacja czasu
                    lastFrameTime = now;
                    return;
                }
                double elapsedSeconds = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                if (gameStarted) {                              // gra aktywna – przesuwaj piłkę
                    ball.updatePosition(elapsedSeconds);
                    if (ball.getBottom() >= 1) {                 // piłka spadła na dół – przegrana
                        endGame(false);
                        return;
                    }
                } else {
                    // jeśli gra jeszcze nie zaczęta – piłka leży na paletce
                    Point2D paddleCenter = new Point2D(
                            paddle.getX() + paddle.getWidth() / 2,
                            paddle.getY() - ball.getHeight()
                    );
                    ball.setPosition(paddleCenter);
                }

                // sprawdzenie odbić od ścian
                if (shouldBallBounceHorizontally()) {
                    ball.bounceHorizontally();
                }
                if (shouldBallBounceVertically()) {
                    ball.bounceVertically();
                }

                // sprawdzenie odbicia od paletki
                if (shouldBallBounceFromPaddle()) {
                    bounceCounter++;
                    if (bounceCounter > maxBounces) {            // zbyt dużo odbić – przegrana
                        endGame(false);
                        return;
                    }
                    double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
                    double ballCenter = ball.getLeft() + ball.getWidth() / 2;
                    double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);
                    ball.bounceFromPaddle(offset);
                }

                // sprawdzenie kolizji z cegłami
                Iterator<Brick> iterator = bricks.iterator();
                while (iterator.hasNext()) {
                    Brick brick = iterator.next();
                    Brick.CrushType result = brick.checkCollision(
                            ball.getLeft(), ball.getRight(), ball.getTop(), ball.getBottom()
                    );
                    if (result != Brick.CrushType.NoCrush) {
                        iterator.remove();
                        score++;
                        if (score >= maxScore) {                 // wygrana
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

                draw();                                          // rysuj następną klatkę
            }
        };
        timer.start();
    }

    // Obsługa końca gry
    private void endGame(boolean won) {
        timer.stop();                                            // zatrzymaj animację
        String msg = won ? "WYGRANA" : "PRZEGRANA";              // ustal komunikat
        String msg2 = msg + "\nWynik: " + score;                 // pełny tekst wyniku

        Platform.runLater(() -> {                                // wywołanie GUI w głównym wątku
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg2, ButtonType.OK);
            alert.setTitle("KONIEC GRY");
            alert.setHeaderText(null);
            alert.showAndWait();

            Platform.exit();                                      // zamknij aplikację
        });
    }

    // Rysowanie punktów i liczby odbić
    private void drawScore(GraphicsContext gc) {
        gc.setFont(Font.font(18));
        gc.setFill(Color.HOTPINK);

        String scoreText = "Punkty: " + score;
        String bounceText = "Odbicia: " + bounceCounter + " / " + maxBounces;

        gc.fillText(scoreText, 10, 20);
        gc.fillText(bounceText, 10, 40);
    }

    // Sprawdzenie czy piłka powinna odbić się od lewej/prawej ściany
    private boolean shouldBallBounceHorizontally() {
        return ball.getX() <= 0 || (ball.getX() + ball.getWidth()) >= 1;
    }

    // Sprawdzenie czy piłka powinna odbić się od górnej ściany
    private boolean shouldBallBounceVertically() {
        return ball.getY() <= 0;
    }

    // Sprawdzenie czy piłka powinna odbić się od paletki
    private boolean shouldBallBounceFromPaddle() {
        return ball.getY() + ball.getHeight() >= paddle.getY() &&
               ball.getY() <= paddle.getY() + paddle.getHeight() &&
               ball.getX() + ball.getWidth() >= paddle.getX() &&
               ball.getX() <= paddle.getX() + paddle.getWidth();
    }

    // Ładowanie poziomu – ustawienie cegieł
    private void loadLevel() {
        Brick.setGridSize(20, 10);                               // ustawia siatkę cegieł
        bricks.clear();                                          // czyści poprzednie cegły

        // Kolory rzędów cegieł
        Color[] rowColors = {
                Color.RED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.BLUE, Color.VIOLET
        };

        // Tworzenie cegieł w wierszach 2–7
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

    // Metoda start – punkt wejścia dla JavaFX
    // Stage primaryStage to główne okno aplikacji
    @Override
    public void start(Stage primaryStage) {
        // Tworzymy obiekt GameCanvas – nasza gra będzie rysowana na tym obszarze
        // 512x512 to rozmiar okna w pikselach
        GameCanvas gameCanvas = new GameCanvas(512, 512);

        // Rysujemy początkowy stan gry (np. tło, cegły, paletkę, piłkę)
        gameCanvas.draw();

        // Układ – StackPane umożliwia łatwe dodanie kanwy
        StackPane root = new StackPane(gameCanvas);

        // Scena JavaFX – "ekran" z naszym root-em
        Scene scene = new Scene(root);

        // Ustawienie tytułu okna
        primaryStage.setTitle("Gra");

        // Podpięcie sceny do okna
        primaryStage.setScene(scene);

        // Pokazanie okna na ekranie
        primaryStage.show();
    }

    // Metoda main – uruchamia aplikację JavaFX
    public static void main(String[] args) {
        launch(args);  // Wywołuje metodę start() w wątku JavaFX
    }
}

```
