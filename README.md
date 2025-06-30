```java
package chat.serwer;

import org.example.serwer.ObslugaKlienta;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Klasa Serwer – główny serwer czatu.
 * Odpowiada za nasłuchiwanie na określonym porcie oraz
 * za uruchamianie wątków do obsługi każdego klienta.
 */
public class Serwer {

    /** Numer portu, na którym serwer będzie nasłuchiwał */
    private final int portSerwera;
    /** ServerSocket do akceptowania połączeń przychodzących */
    private ServerSocket gniazdoSerwera;
    /** Lista strumieni wyjściowych do wysyłania wiadomości do klientów */
    private final List<PrintWriter> listaStrumieniWyjsciowych;

    /**
     * Konstruktor serwera
     * @param port Port, na którym serwer będzie nasłuchiwał
     */
    public Serwer(int port) {
        this.portSerwera = port;
        // Tworzymy zsynchronizowaną listę, by bezpiecznie dodawać/usuwać strumienie z wielu wątków
        this.listaStrumieniWyjsciowych = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Uruchamia serwer: otwiera gniazdo i w pętli akceptuje klientów.
     */
    public void uruchom() {
        try {
            // Inicjalizacja ServerSocket
            gniazdoSerwera = new ServerSocket(portSerwera);
            System.out.println("Serwer uruchomiony na porcie " + portSerwera);

            // Pętla główna: przyjmujemy nowych klientów
            while (true) {
                Socket gniazdoKlienta = gniazdoSerwera.accept();
                System.out.println("Nowe połączenie od " + gniazdoKlienta.getInetAddress());

                // Dla każdego klienta uruchamiamy wątek obsługi
                ObslugaKlienta obsluga = new ObslugaKlienta(gniazdoKlienta, listaStrumieniWyjsciowych);
                obsluga.start();
            }
        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Sprzątanie: zamknięcie gniazda serwera
            if (gniazdoSerwera != null && !gniazdoSerwera.isClosed()) {
                try {
                    gniazdoSerwera.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Punkt startowy aplikacji serwerowej.
     * Ustawia port na 12345 i wywołuje uruchomienie.
     */
    public static void main(String[] args) {
        int domyslnyPort = 12345;
        Serwer serwer = new Serwer(domyslnyPort);
        serwer.uruchom();
    }
}
```
```java
package org.example.serwer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

    /**
     * Wątek obsługujący pojedyncze połączenie klienta.
     * Odbiera wiadomości od klienta i przekazuje je do wszystkich pozostałych.
     */
    public class ObslugaKlienta extends Thread {

        /** Gniazdo sieciowe klienta */
        private final Socket gniazdoKlienta;
        /** Strumień do zapisu na gniazdku klienta */
        private PrintWriter pisarz;
        /** Strumień do odczytu z gniazdka klienta */
        private BufferedReader czytelnik;
        /** Nick (nazwa) klienta, podany przy połączeniu */
        private String nickKlienta;
        /** Wspólna lista wszystkich strumieni wyjściowych klientów */
        private final List<PrintWriter> listaStrumieniWyjsciowych;

        /**
         * Konstruktor wątku obsługi klienta.
         * @param gniazdo  Połączenie z klientem
         * @param lista   Lista strumieni wyjściowych do broadcastu
         */
        public ObslugaKlienta(Socket gniazdo, List<PrintWriter> lista) {
            this.gniazdoKlienta = gniazdo;
            this.listaStrumieniWyjsciowych = lista;
        }

        @Override
        public void run() {
            try {
                // Inicjalizacja strumieni
                czytelnik = new BufferedReader(new InputStreamReader(gniazdoKlienta.getInputStream()));
                pisarz = new PrintWriter(gniazdoKlienta.getOutputStream(), true);

                // Pierwsza wiadomość od klienta to jego nick
                nickKlienta = czytelnik.readLine();
                // Dodajemy strumień do listy, by móc mu wysyłać wiadomości
                listaStrumieniWyjsciowych.add(pisarz);

                // Informujemy wszystkich o dołączeniu nowego użytkownika
                broadcast("*** " + nickKlienta + " dołączył do czatu! ***");

                // Główna pętla: czytamy kolejne linie od klienta
                String wiadomosc;
                while ((wiadomosc = czytelnik.readLine()) != null) {
                    // Przy każdej otrzymanej wiadomości robimy broadcast
                    broadcast(nickKlienta + ": " + wiadomosc);
                }
            } catch (IOException e) {
                System.err.println("Błąd w obsłudze klienta " + nickKlienta + ": " + e.getMessage());
            } finally {
                // Sprzątanie przy rozłączeniu klienta
                try {
                    listaStrumieniWyjsciowych.remove(pisarz);
                    broadcast("*** " + nickKlienta + " opuścił czat. ***");
                    if (gniazdoKlienta != null && !gniazdoKlienta.isClosed()) {
                        gniazdoKlienta.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Wysyła podaną wiadomość do wszystkich klientów.
         * @param wiadomosc Tekst do rozesłania
         */
        private void broadcast(String wiadomosc) {
            synchronized (listaStrumieniWyjsciowych) {
                for (PrintWriter p : listaStrumieniWyjsciowych) {
                    p.println(wiadomosc);
                }
            }
        }
    }

}
```
```java
package chat.klient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Klasa Klient – zarządza połączeniem z serwerem czatu.
 * Umożliwia wysyłanie wiadomości oraz uruchomienie wątku nasłuchującego.
 */
public class Klient {

    /** Adres hosta serwera (np. "localhost") */
    private final String adresSerwera;
    /** Port, na którym serwer nasłuchuje */
    private final int portSerwera;
    /** Nick użytkownika */
    private final String nick;
    /** Główne gniazdo sieciowe klienta */
    private Socket gniazdo;
    /** Strumień do wysyłania wiadomości do serwera */
    private PrintWriter pisarzDoSerwera;
    /** Strumień do odczytywania wiadomości od serwera */
    private BufferedReader czytelnikSerwera;
    /** Referencja do GUI, by przekazywać odebrane wiadomości */
    private final chat.klient.GUIKlient gui;

    /**
     * Konstruktor klienta.
     * Otwiera połączenie z serwerem i wysyła swój nick.
     * @param adres  Adres serwera
     * @param port   Port serwera
     * @param nick   Nick użytkownika
     * @param gui    Obiekt GUI do wyświetlania przychodzących wiadomości
     * @throws IOException Jeśli nie można nawiązać połączenia
     */
    public Klient(String adres, int port, String nick, chat.klient.GUIKlient gui) throws IOException {
        this.adresSerwera = adres;
        this.portSerwera = port;
        this.nick = nick;
        this.gui = gui;

        // Nawiązujemy połączenie TCP/IP
        gniazdo = new Socket(adresSerwera, portSerwera);
        // Inicjalizujemy strumienie
        pisarzDoSerwera = new PrintWriter(gniazdo.getOutputStream(), true);
        czytelnikSerwera = new BufferedReader(new InputStreamReader(gniazdo.getInputStream()));

        // Wysyłamy swój nick jako pierwszą wiadomość
        pisarzDoSerwera.println(nick);
    }

    /**
     * Wysyła treść wiadomości do serwera.
     * @param wiadomosc Tekst wiadomości
     */
    public void wyslijWiadomosc(String wiadomosc) {
        pisarzDoSerwera.println(wiadomosc);
    }

    /**
     * Uruchamia wątek nasłuchujący komunikaty z serwera.
     */
    public void rozpocznijNasluchiwanie() {
        new chat.klient.NasluchiwaczSerwera(czytelnikSerwera, gui).start();
    }
}
```
```java
package chat.klient;

import java.io.BufferedReader;
import java.io.IOException;
import javafx.application.Platform;

/**
 * Wątek nasłuchujący komunikaty z serwera.
 * Otrzymane linie przekazuje do GUI w wątku JavaFX.
 */
public class NasluchiwaczSerwera extends Thread {

    /** Strumień do odczytu wiadomości od serwera */
    private final BufferedReader czytelnikSerwera;
    /** Referencja do GUI, by wyświetlać przychodzące wiadomości */
    private final chat.klient.GUIKlient gui;

    /**
     * Konstruktor wątku nasłuchującego.
     * @param czytelnik  Strumień czytający z serwera
     * @param gui        Obiekt GUI do aktualizacji
     */
    public NasluchiwaczSerwera(BufferedReader czytelnik, chat.klient.GUIKlient gui) {
        this.czytelnikSerwera = czytelnik;
        this.gui = gui;
    }

    @Override
    public void run() {
        String linia;
        try {
            // Czytamy kolejne linie aż do rozłączenia
            while ((linia = czytelnikSerwera.readLine()) != null) {
                final String wiadomosc = linia;
                // Aktualizacja GUI musi iść przez Platform.runLater
                Platform.runLater(() -> gui.dodajWiadomosc(wiadomosc));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
```java
package chat.klient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;

/**
 * Proste GUI klienta czatu w JavaFX.
 * Wyświetla obszar z wiadomościami, pole wpisywania i przycisk “Wyślij”.
 */
public class GUIKlient extends Application {

    /** Pole tekstowe do wyświetlania całego czatu */
    private TextArea obszarWiadomosci;
    /** Pole tekstowe do wpisywania własnych wiadomości */
    private TextField poleWpisywania;
    /** Przycisk do wysyłania wiadomości */
    private Button przyciskWyslij;
    /** Obiekt Klient zarządzający połączeniem */
    private chat.klient.Klient klient;

    @Override
    public void start(Stage primaryStage) {
        // --- Inicjalizacja komponentów GUI ---
        obszarWiadomosci = new TextArea();
        obszarWiadomosci.setEditable(false); // tylko do odczytu

        poleWpisywania = new TextField();
        przyciskWyslij = new Button("Wyślij");

        // Akcja po naciśnięciu przycisku lub klawisza Enter
        przyciskWyslij.setOnAction(e -> wyslijWiadomosc());
        poleWpisywania.setOnAction(e -> wyslijWiadomosc());

        VBox layout = new VBox(5, obszarWiadomosci, poleWpisywania, przyciskWyslij);
        Scene scena = new Scene(layout, 400, 300);

        primaryStage.setScene(scena);
        primaryStage.setTitle("Czat klient");
        primaryStage.show();

        // --- Pobranie nicku użytkownika ---
        TextInputDialog dialogNick = new TextInputDialog("Anonim");
        dialogNick.setTitle("Ustaw nick");
        dialogNick.setHeaderText("Podaj swój nick:");
        dialogNick.setContentText("Nick:");
        String nick = dialogNick.showAndWait().orElse("Anonim");

        // --- Nawiązanie połączenia z serwerem ---
        try {
            String host = "localhost";
            int port = 12345;
            klient = new Klient(host, port, nick, this);
            klient.rozpocznijNasluchiwanie();
        } catch (Exception ex) {
            ex.printStackTrace();
            // Jeśli błąd, zamykamy aplikację
            Platform.exit();
        }
    }

    /**
     * Wysyła zawartość pola wpisywania do serwera i czyści pole.
     */
    private void wyslijWiadomosc() {
        String tekst = poleWpisywania.getText().trim();
        if (!tekst.isEmpty()) {
            klient.wyslijWiadomosc(tekst);
            poleWpisywania.clear();
        }
    }

    /**
     * Dodaje otrzymaną wiadomość do obszaru czatu.
     * @param wiadomosc Tekst otrzymanej wiadomości
     */
    public void dodajWiadomosc(String wiadomosc) {
        obszarWiadomosci.appendText(wiadomosc + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```
