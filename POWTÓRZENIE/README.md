# POWTÓRZENIE
### pom.xml
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>powtoreczka</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>24</maven.compiler.source>
        <maven.compiler.target>24</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>20</javafx.version>
        <javafx.platform>win</javafx.platform>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.43.2.2</version>
        </dependency>

        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
            <classifier>${javafx.platform}</classifier>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
            <classifier>${javafx.platform}</classifier>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <executions>
                    <execution>
                        <id>default-cli</id>
                        <goals>
                            <goal>run</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <mainClass>org.example.circleapp.Main</mainClass>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <executions>
                    <execution>
                        <goals><goal>java</goal></goals>
                    </execution>
                </executions>
                <configuration>
                    <mainClass>org.example.circleapp.Main</mainClass>
                    <includeProjectDependencies>true</includeProjectDependencies>
                    <classpathScope>compile</classpathScope>
                    <arguments>
                        <argument>--module-path</argument>
                        <argument>${project.build.directory}/classes;${user.home}/.m2/repository/org/openjfx/javafx-controls/20/javafx-controls-20-win.jar;…</argument>
                        <argument>--add-modules</argument>
                        <argument>javafx.controls,javafx.fxml</argument>
                    </arguments>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
### Klasa ServerThread
```java
package org.example.circleapp.client;

import org.example.circleapp.shared.Dot;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerThread implements Runnable {
    // Socket TCP do komunikacji z serwerem
    private Socket socket;

    // Czytnik do odbierania tekstowych wiadomości z serwera
    private BufferedReader in;

    // Writer do wysyłania wiadomości do serwera
    private PrintWriter out;

    // Funkcja (callback), którą aplikacja ustawi, żeby odbierać przychodzące kropki
    private Consumer<Dot> dotConsumer;

    // Konstruktor – nawiązuje połączenie z serwerem pod podanym adresem i portem
    public ServerThread(String address, int port) throws IOException {
        socket = new Socket(address, port);                     // otwarcie połączenia TCP
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // strumień wejściowy
        out = new PrintWriter(socket.getOutputStream(), true);  // strumień wyjściowy z autoflush
    }

    // Setter na funkcję konsumenta – ustawia kto będzie przetwarzał przychodzące Dot-y
    public void setDotConsumer(Consumer<Dot> dotConsumer) {
        this.dotConsumer = dotConsumer;
    }

    // Wysyła kropkę (dot) do serwera – konwertuje do tekstu
    public void send(String color, double radius, double x, double y) {
        Dot dot = new Dot(x, y, radius, color);  // tworzy obiekt Dot z parametrami
        String message = dot.toMessage();        // zamienia na format tekstowy
        out.println(message);                    // wysyła tekst przez socket
    }

    // Główna pętla wątku – odbiera wiadomości z serwera
    @Override
    public void run() {
        try {
            String msg;
            // Czytaj linia po linii dopóki serwer nie zamknie połączenia
            while ((msg = in.readLine()) != null) {
                Dot dot = Dot.fromMessage(msg);    // zamień tekst na obiekt Dot
                System.out.println(dot);           // debug: wypisz na konsolę
                if (dotConsumer != null) {         // jeśli mamy ustawionego odbiorcę
                    dotConsumer.accept(dot);       // przekaż mu odebrany Dot
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();                    // zawsze zamykaj socket na końcu
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

```
### Klasa ClientThread
```java
package org.example.circleapp.server;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable {
    // Referencja do głównego serwera – pozwala na wywoływanie jego metod (np. broadcast)
    private Server server;

    // Socket TCP dla tego klienta
    private Socket socket;

    // Do odczytu linii tekstu od klienta
    private BufferedReader in;

    // Do wysyłania linii tekstu do klienta
    private PrintWriter out;

    // Konstruktor – przyjmuje serwer i socket klienta
    public ClientThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            // Tworzy strumienie wejścia i wyjścia do komunikacji tekstowej
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Wysyła pojedynczą wiadomość tekstową do klienta
    public void sendMessage(String message) {
        out.println(message);
    }

    // Główna pętla wątku – nasłuchuje przychodzących wiadomości
    @Override
    public void run() {
        try {
            String msg;
            // Odbiera linie od klienta i przekazuje do broadcastu serwera
            while ((msg = in.readLine()) != null) {
                server.broadcast(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();       // zamyka połączenie socketowe
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Informuje serwer, że klient się rozłączył
            server.removeClient(this);
        }
    }
}
```
### Klasa Server
```java
package org.example.circleapp.server;

import org.example.circleapp.shared.Dot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;                          // Port na którym serwer nasłuchuje
    private ServerSocket serverSocket;         // Socket nasłuchujący na klientów
    private final List<ClientThread> clients = new ArrayList<>(); // Lista wszystkich klientów

    // Połączenie do bazy danych (SQLite)
    private Connection connection;

    // Konstruktor serwera – ustawia port
    public Server(int port) {
        this.port = port;
    }

    // Startuje serwer – główna pętla nasłuchiwania
    public void start() {
        connect();    // otwarcie połączenia z bazą i stworzenie tabeli

        try {
            serverSocket = new ServerSocket(port);  // otwarcie gniazda nasłuchującego

            while (true) {
                Socket clientSocket = serverSocket.accept(); // blokuje i czeka na nowego klienta

                // Tworzy nowy wątek obsługi klienta
                ClientThread clientThread = new ClientThread(this, clientSocket);
                clients.add(clientThread);

                // Wyślij temu klientowi wszystkie doty z bazy na start
                sendAllDotsTo(clientThread);

                new Thread(clientThread).start(); // uruchamia klienta w osobnym wątku
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Wysyła wiadomość do wszystkich klientów
    public void broadcast(String message) {
        Dot dot = Dot.fromMessage(message);   // Parsuje wiadomość na obiekt Dot
        saveDot(dot);                         // Zapisuje do bazy

        // Wysyła wiadomość do wszystkich klientów
        for (ClientThread client : clients) {
            client.sendMessage(message);
        }
    }

    // Usuwa klienta z listy po rozłączeniu
    public void removeClient(ClientThread clientThread) {
        clients.remove(clientThread);
    }

    // Połączenie z bazą SQLite i utworzenie tabeli jeśli nie istnieje
    private void connect() {
        try {
            // Otwiera połączenie JDBC z bazą SQLite (plikowa baza)
            connection = DriverManager.getConnection("jdbc:sqlite:circles.db");

            try (Statement stmt = connection.createStatement()) {
                // Tworzy tabelę jeśli nie istnieje
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS dot (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "x INTEGER NOT NULL, " +
                            "y INTEGER NOT NULL, " +
                            "color TEXT NOT NULL, " +
                            "radius INTEGER NOT NULL)"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Zapisuje kropkę do bazy danych
    public void saveDot(Dot dot) {
        String sql = "INSERT INTO dot(x, y, color, radius) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, (int) dot.x());
            pstmt.setInt(2, (int) dot.y());
            pstmt.setString(3, dot.color());
            pstmt.setInt(4, (int) dot.radius());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Pobiera wszystkie zapisane doty z bazy
    public List<Dot> getSavedDots() {
        List<Dot> dots = new ArrayList<>();
        String sql = "SELECT x, y, color, radius FROM dot";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double x = rs.getInt("x");
                double y = rs.getInt("y");
                double radius = rs.getInt("radius");
                String color = rs.getString("color");
                dots.add(new Dot(x, y, radius, color));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dots;
    }

    // Wysyła wszystkie zapisane doty do nowego klienta
    public void sendAllDotsTo(ClientThread client) {
        List<Dot> allDots = getSavedDots();

        for (Dot dot : allDots) {
            client.sendMessage(dot.toMessage());
        }
    }
}
```
### Klasa Dot
```java
package org.example.circleapp.shared;

public record Dot(double x, double y, double radius, String color) {
    // Konwersja obiektu Dot do formatu tekstowego (do wysyłki przez sieć)
    public String toMessage() {
        // Format: kolor;promień;x;y
        return String.format("%s;%f;%f;%f", color, radius, x, y);
    }

    // Parsowanie tekstu z formatu wiadomości na obiekt Dot
    public static Dot fromMessage(String message) {
        String[] parts = message.split(";");  // dzieli tekst na części po średnikach

        // Zamienia części na odpowiednie typy i tworzy nowy obiekt Dot
        return new Dot(
            Double.parseDouble(parts[2]),   // x
            Double.parseDouble(parts[3]),   // y
            Double.parseDouble(parts[1]),   // radius
            parts[0]                        // color
        );
    }
}
```
### Klasa Controller
```java
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
    // Komponenty zdefiniowane w pliku FXML
    @FXML private Canvas canvas;           // obszar do rysowania kropek
    @FXML private ColorPicker colorPicker; // wybór koloru
    @FXML private Slider radiusSlider;     // wybór promienia

    private final ServerThread serverThread;   // referencja do klienta sieciowego
    private GraphicsContext graphics;          // kontekst graficzny do rysowania na kanwie

    // Konstruktor – przyjmuje obiekt ServerThread (połączenie z serwerem)
    public Controller(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    // Metoda wywoływana automatycznie po wczytaniu FXML
    @FXML
    public void initialize() {
        graphics = canvas.getGraphicsContext2D(); // pobiera kontekst graficzny

        // Ustawia funkcję odbierającą przychodzące kropki z serwera
        serverThread.setDotConsumer(dot -> {
            // Rysowanie musi być na wątku GUI
            Platform.runLater(() -> {
                graphics.setFill(Color.web(dot.color()));                   // ustawia kolor
                graphics.fillOval(
                        dot.x() - dot.radius(), dot.y() - dot.radius(),    // lewy górny róg
                        dot.radius() * 2, dot.radius() * 2                 // szerokość i wysokość
                );
            });
        });
    }

    // Obsługa kliknięcia myszką na kanwie
    @FXML
    private void onMouseClicked(MouseEvent event) {
        double x = event.getX();                    // współrzędna X kliknięcia
        double y = event.getY();                    // współrzędna Y kliknięcia
        double r = radiusSlider.getValue();         // wartość promienia z suwaka
        Color c = colorPicker.getValue();           // wybrany kolor z palety

        // Konwersja koloru na HEX String (np. #FF00FF)
        String colorStr = String.format(
                "#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255)
        );

        // Najpierw lokalnie rysujemy na swoim Canvas
        graphics.setFill(c);
        graphics.fillOval(x - r, y - r, r * 2, r * 2);

        // Wysyłamy dane o kropce na serwer
        serverThread.send(colorStr, r, x, y);
    }

    // Przyciski do uruchamiania serwera/klienta (niewykorzystane w tej wersji)
    @FXML private void onStartServerClicked() {}
    @FXML private void onConnectClicked() {}
}
```
### Klasa Main
```java
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
        int port = 5000;                       // ustalony port serwera
        String address = "localhost";          // adres lokalny

        // Startujemy serwer w tle (osobny wątek)
        Server server = new Server(port);
        Thread srv = new Thread(server::start);
        srv.setDaemon(true);                   // zakończy się razem z aplikacją
        srv.start();

        // Próba połączenia klienta do serwera
        ServerThread client = null;
        for (int i = 0; i < 10; i++) {
            try {
                client = new ServerThread(address, port);
                break;
            } catch (Exception e) {
                Thread.sleep(100);             // jeśli serwer jeszcze nie gotowy – czekamy chwilę
            }
        }

        if (client == null) {
            throw new RuntimeException();      // nie udało się połączyć
        }

        // Uruchamiamy klienta w osobnym wątku
        Thread cli = new Thread(client);
        cli.setDaemon(true);
        cli.start();

        // Wczytujemy widok z FXML i przypisujemy mu kontroler z naszym klientem
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app-view.fxml"));
        ServerThread finalClient = client;
        loader.setControllerFactory(c -> new Controller(finalClient));
        Parent root = loader.load();

        // Konfiguracja sceny i okna
        stage.setTitle("Rysowanie kółek");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Punkt startowy aplikacji JavaFX
    public static void main(String[] args) {
        launch(args); // wywołuje metodę start()
    }
}
```
