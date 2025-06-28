# TESTY
### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>site</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>24</maven.compiler.source>
        <maven.compiler.target>24</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.42.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
        <dependency>
            <groupId>at.favre.lib</groupId>
            <artifactId>bcrypt</artifactId>
            <version>0.9.0</version>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-inline</artifactId>
            <version>5.2.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>5.2.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>RELEASE</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>5.9.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M9</version>
            </plugin>
        </plugins>
    </build>
</project>
```
### Zadanie 1a
Utwórz rekord Song składający się z napisów: artysty i tytułu oraz czasu trwania wyrażonego w sekundach. Utwórz klasę Playlist dziedziczącą po ArrayList<Song>. 
```java
package auth;

import database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public record Song(int id, String artist, String title, int durationSeconds) {
    public static class Persistence {

        public static Optional<Song> read(int songId) throws SQLException {
            String sql = "SELECT id, artist, title, duration_seconds " + "FROM songs WHERE id = ?";

            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setInt(1, songId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return Optional.of(new Song(
                                rs.getInt("id"),
                                rs.getString("artist"),
                                rs.getString("title"),
                                rs.getInt("duration_seconds")
                        ));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }
}

```
`Persistance` - wewnętrzna pomocnicza klasa grupująca operacje na bazie danych.


Przygotowanie zapytania SQL do bazy.
```java
         String sql = "SELECT id, artist, title, duration_seconds " + "FROM songs WHERE id = ?";
```
Tworzymy `PreparedStatement`, łączymy się z bazą danych i przygotowywujemy się do wysyłania zapytań. Dlaczego `PreparedStatement` a nie zwykły `Statement`? Głownie dlatego że będziemy wysyłać do bazy wiele zapytań.
```java
         try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
```
Jest to skrócony zapis, normalny wyglądałby tak:
```java
         Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
```
Metoda wstawia wartość typu `int` we wskazane miejsce, `1` oznacza "w miejscu pierwszego pojawienia się `?`", a `songId` wartość parametru, co daje nam w tym przypadku np. `...WHERE id = 42`
```java
                ps.setInt(1, songId);
```
`ResultSet rs` przetrzymuję wyniki z zapytania, `rs.next()` przesuwa "kursor" na kolejny wynik i zwraca `true` jeśli istnieje lub `false`. U nas jeśli będzie `true` to tworzy "kontener" `Optional` z pobranymi wartościami i zwraca go, w innym przypadku zwraca pusty "kontener". Na koniec nie zamykamy manulanie `ResultSet rs`, robi to za nas `try{...}`.
```java
                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return Optional.of(new Song(
                                rs.getInt("id"),
                                rs.getString("artist"),
                                rs.getString("title"),
                                rs.getInt("duration_seconds")
                        ));
                    } else {
                        return Optional.empty();
                    }
```
## Klasa `Tests` ze wszystkimi testami.
```java
package tests;

import auth.*;
import database.DatabaseConnection;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.testng.Assert;

import javax.naming.AuthenticationException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static auth.Account.Persistence.register;


public class Tests {
    @BeforeTest
    public static void connect(){
        DatabaseConnection.connect("song.db", "supercool");
    }

    @AfterTest
    public static void disconnect(){
        DatabaseConnection.disconnect("supercool");

    }

    //1b
    @Test
    public void isPlaylistEmpty(){
        Playlist playlist = new Playlist();

        Assert.assertTrue(playlist.isEmpty());
    }

    //1c
    @Test
    public void isPlaylistSizeOne() {
        Playlist playlist = new Playlist();
        Song song = new Song(1,"ArtistX", "TitleY", 123);
        playlist.add(song);

        Assert.assertTrue(playlist.size() == 1);
    }

    //1d i 1e
    @Test
    public void sameSongInPlaylist() {
        Playlist playlist = new Playlist();
        Song song = new Song(2, "ArtistX", "TitleY", 123);
        playlist.add(song);

        for(Song s : playlist) {
            Assert.assertTrue(s == song);
        }
    }

    //1f
    @Test
    public void chosenSong() {
        Playlist playlist = new Playlist();

        playlist.add(new Song(1,"ABC", "XYZ", 20));
        playlist.add(new Song(2,"DEF", "YXZ", 19));
        playlist.add(new Song(3,"GHI", "ZYX", 35));
        playlist.add(new Song(4,"JKL", "XZY", 71));

        Assert.assertTrue(playlist.atSecond(41) == playlist.get(3));
    }

    // 1g i 1h
    @Test
    public void negativeSecondsException(){
        Playlist playlist = new Playlist();

        Assert.assertEquals("Ujemny czas!", playlist.atSecond(-15));
    }

    @Test
    public void secondsOutOfPlaylistException(){
        Playlist playlist = new Playlist();

        Assert.assertEquals("Podany czas nie mieści się!", playlist.atSecond(2137));
    }

    // 2b
    @Test
    public  void readSongWihExistingIndex() throws SQLException {
        Playlist playlist = new Playlist();
        playlist.add(new Song(8, "Young Mlody", "what", 69));

        Assert.assertEquals(playlist.get(8), Song.Persistence.read(8));
    }

    // 2c
    @Test
    public  void readSongWihNoExistingIndex() throws SQLException {
        Playlist playlist = new Playlist();
        playlist.add(new Song(8, "Young Mlody", "what", 69));

        Assert.assertEquals(playlist.get(9), Song.Persistence.read(8));
    }

    // 2d
    // test sparametryzowany oznacza że jedna metoda testowa zostanie wykonana wielokrotnie, każdorazowo z innymi danymi wejściowym i oczekiwanym wynikiem
    @ParameterizedTest
    @MethodSource("provideForTesting")
    /* wcześniejsza zła metoda, porównujemy int z obiektem co zawsze zwróci fałsz
    void indexStream(int index, Song expectedSong) {
        Assert.assertEquals(index, expectedSong);
    }
    */

    //poprawiona metoda, pobieramy piosenke względem indeksu i porównujemy
    void indexStream(int index, Song expectedSong) throws SQLException {
        Optional<Song> maybe = Song.Persistence.read(index);
        Assert.assertTrue(maybe.isPresent()); //sprawdzamy czy piosenka o danym indeksie w ogóle istnieje

        Song song = maybe.get();

        Assert.assertEquals(expectedSong, song);
    }
    static Stream<Arguments> provideForTesting() {
        return Stream.of(
                Arguments.of(1, new Song(1, "Artist1", "Title1", 10)),
                Arguments.of(2, new Song(2, "Artist2", "Title2", 20)),
                Arguments.of(3, new Song(3, "Artist3", "Title3", 30)),
                Arguments.of(4, new Song(4, "Artist4", "Title4", 40))
        );
    }

    // 2e
    @ParameterizedTest
    @CsvFileSource(resources = "/songs.csv", numLinesToSkip = 1)
    void readFromCsv(int id, String artist, String title, int length) throws SQLException {
        Optional<Song> maybe = Song.Persistence.read(id);
        Assert.assertTrue(maybe.isPresent());

        String artistDB = maybe.get().artist();
        String titleDB = maybe.get().title();
        int lengthDB = maybe.get().durationSeconds();

        Assert.assertEquals(artist, artistDB);
        Assert.assertEquals(title, titleDB);
        Assert.assertEquals(length, lengthDB);
    }

    // 3a
    @Test
    void registerCheck() {
        // za pomocą INSERT(...) dodajemy do bazy nowego użytkownika i do userId dostajemy numer kolumny
        int userId = register("userTest", "passwordTest");

        Assert.assertTrue( userId > 0);
    }

    // 3b
    @Test
    void loginCheck() throws AuthenticationException, SQLException {
        AccountManager ac = new AccountManager();
        int userId = register("userTest", "passwordTest");

        boolean auth = ac.authenticate("userTest", "passwordTest");
        Assert.assertTrue(auth);
    }


}

```
### Zadanie 1b
Napisz test sprawdzający, czy nowo utworzona playlista jest pusta.
```java
    @Test
    public void isPlaylistEmpty(){
        Playlist playlist = new Playlist();

        Assert.assertTrue(playlist.isEmpty());
    }
```
Tworzymy nowy obiekt `playlist` (Klasa Playlist rozszerza *ArrayList<Song>*), i spradzamy czy jest pusta. 


`Assert.assertTrue(playlist.isEmpty());` - można przetłumaczyć dla łatwiejszego zrozumienia - "czy to prawda, że `playlist` jest pusta?".
### Zadanie 1c
Napisz test sprawdzający, czy po dodaniu jednego utworu playlista ma rozmiar 1.
```java
    @Test
    public void isPlaylistSizeOne() {
        Playlist playlist = new Playlist();
        Song song = new Song(1,"ArtistX", "TitleY", 123);
        playlist.add(song);

        Assert.assertTrue(playlist.size() == 1);
    }
```
Ponownie tworzymy nowy obiekt `playlist` i na dodatek nowy obiekt `Song` z parametrami (id, nazwa_artysty, tytuł_piosenki, czas_trwania_piosenki), dodajemy tę piosenkę do `playlist`.


`Assert.assertTrue(playlist.size() == 1);` zwraca `true` jeśli rozmiar `playlist` wynosi `1`;
### Zadanie 1d
Napisz test sprawdzający, czy po dodaniu jednego utworu, jest w nim ten sam utwór.
```java
    @Test
    public void sameSongInPlaylist() {
        Playlist playlist = new Playlist();
        Song song = new Song(2, "ArtistX", "TitleY", 123);
        playlist.add(song);

        for(Song s : playlist) {
            Assert.assertTrue(s == song);
        }
    }
```
Tak samo jak wcześniej tworzymy dwa obiekty `song` i `playlist`, dodajemy jedno do drugiego. W pętli "przechodzimy się" obiektem po obiekcie, i dostajemy `true` jeśli jakaś piosenka `s` znajduję się już w `playlist`. 
### Zadanie 1f
W klasie Playlist napisz metodę atSecond, która przyjmie całkowitą liczbę sekund i zwróci obiekt Song, który jest odtwarzany po tylu sekundach odtwarzania playlisty. Napisz test sprawdzający działanie tej metody.
```java
    public Song atSecond(int seconds){
        if(seconds < 0) {
            throw new IndexOutOfBoundsException("Ujemny czas!");
        }

        int totalSeconds = 0;
        for(Song s : this){
            totalSeconds += s.durationSeconds();
        }
        if(seconds >= totalSeconds) throw new IndexOutOfBoundsException("Podany czas nie mieści się!");

        int sum = 0;
        for(Song s : this) {
            sum += s.durationSeconds();
            if(seconds < sum) {
                return s;
            }
        }

        return null;
    }         
```
```java
    @Test
    public void chosenSong() {
        Playlist playlist = new Playlist();

        playlist.add(new Song(1,"ABC", "XYZ", 20));
        playlist.add(new Song(2,"DEF", "YXZ", 19));
        playlist.add(new Song(3,"GHI", "ZYX", 35));
        playlist.add(new Song(4,"JKL", "XZY", 71));

        Assert.assertTrue(playlist.atSecond(41) == playlist.get(3));
    }
```
Metoda `atSecond(int seconds)` przyjmuję liczbę sekund, dzięki której będziemy mogli sprawdzić która piosenka teraz "leci". W drugiej pętli sumujemy liczbę sekund do `sum`, i jeżeli `sum` będzie większe od podanej liczby sekund, znaczy to że znaleźliśmy piosenkę która teraz by była odtwarzana. `Assert.assertTrue(playlist.atSecond(41) == playlist.get(3));` liczba sekund to 41, gdy dodamy liczbę sekund pierwszych dwóch piosenek wyjdzie nam 39, dlatego zwróconą piosenką przez `atSecond()` powinna być piosenka numer trzy.
### Zadania 1g i 1h
Zmodyfikuj metodę atSecond tak, aby rzucała wyjątek IndexOutOfBoundsException, jeżeli zadany czas jest późniejszy niż czas odtwarzania playlisty. Napisz test sprawdzający rzucanie tego wyjątku.


Zmodyfikuj metodę atSecond, aby rozróżniała podanie ujemnego czasu i czasu wykraczającego poza czas trwania listy i zapisywała w argumencie message konstruktora IndexOutOfBoundsException odpowiedni napis. Napisz dwa testy sprawdzające każdy z wymienionych przypadków.
```java
@Test
public void negativeSecondsException(){
    Playlist playlist = new Playlist();
    IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, () -> playlist.atSecond(-15));

    assertEquals("Ujemny czas!", ex.getMessage());
}

@Test
public void secondsOutOfPlaylistException(){
    Playlist playlist = new Playlist();
    IndexOutOfBoundsException ex = assertThrows(IndexOutOfBoundsException.class, () -> playlist.atSecond(1));

    assertEquals("Podany czas nie mieści się!", ex.getMessage());
}
```
W metodzie wyżej zostały dodane dwa warunki, jeden gdy użytkownik poda minusową liczbę sekund, i drugi gdy liczba sekund będzie większa niż liczba sekund wszystkich piosenek.


W testach `Assert.assertEquals("1, 2");` sprawdzamy czy, metoda zwróci odpowiedni komunikat.
### Zadanie 2a.
Zapoznaj się z bazą danych songs.db oraz plikiem songs.csv zawierającymi te same dane. W rekordzie Song stwórz publiczną, statyczną klasę Persistence. W tej klasie powinna znaleźć się metoda read, która przyjmuje indeks, a zwraca obiekt Optional<Song>, zapełniony lub nie, w zależności od poprawności indeksu w bazie.


Metoda zaimplementowana wyżej.

### Zadanie 2b
Napisz test sprawdzający odczyt z bazy danych piosenki o poprawnym indeksie.
```java
    @Test
    public  void readSongWihExistingIndex() throws SQLException {
        Playlist playlist = new Playlist();
        playlist.add(new Song(8, "Young Mlody", "what", 69));

        Assert.assertEquals(playlist.get(8), Song.Persistence.read(8));
    }
```
Test `readSongWihExistingIndex()` sprawdza czy piosenka "wzięta" manualnia przez nas `playlist.get(8)`, to ta sama piosenka którą zwróci zapytanie w metodzie `read()` przy podstawieniu tego samego indeksu.
### Zadanie 2c
Napisz test sprawdzający próbę odczytu piosenki i niepoprawnym indeksie. Wydziel łączenie i rozłączanie się z bazą do oddzielnych metod i nadaj im odpowiednie adnotacje.
```java
@Test
public void readSongWithNonExistingIndex() throws SQLException {
    Optional<Song> maybe = Song.Persistence.read(99999);
    assertTrue(maybe.isEmpty());
}
```
### Zadanie 2d
Napisz test sparametryzowany metodą zwracającą strumień indeksów i oczekiwanych piosenek.
```java
    @ParameterizedTest
    @MethodSource("provideForTesting")
    /* wcześniejsza zła metoda, porównujemy int z obiektem co zawsze zwróci fałsz
    void indexStream(int index, Song expectedSong) {
        Assert.assertEquals(index, expectedSong);
    }
    */

    void indexStream(int index, Song expectedSong) throws SQLException {
        Optional<Song> maybe = Song.Persistence.read(index);
        Assert.assertTrue(maybe.isPresent());

        Song song = maybe.get();

        Assert.assertEquals(expectedSong, song);
    }
    static Stream<Arguments> provideForTesting() {
        return Stream.of(
                Arguments.of(1, new Song(1, "Artist1", "Title1", 10)),
                Arguments.of(2, new Song(2, "Artist2", "Title2", 20)),
                Arguments.of(3, new Song(3, "Artist3", "Title3", 30)),
                Arguments.of(4, new Song(4, "Artist4", "Title4", 40))
        );
    }
```
Test sparametryzowany oznacza, że jedna metoda testowa zostanie wykonana wielkrotnie, każdorazowo z innymi danymi wejściowymi i oczekiwanym wynikiem.


`@MethodSource("provideForTesting")` wskazujemy na metodę która będzie nam dostarczać dane do testów. Tworzymy zmienną `maybe` do który odczytujemy piosenkę o podanym indeksie, i sprawdzamy czy w ogóle taka istnieje 
- `Assert.assertTrue(maybe.isPresent());`, i zapisujemy ją do `song`. `Assert.assertEquals(expectedSong, song);` sprawdza czy oczekiwana przez nas piosenka równa się tej o podanym przez nas indeksie.
### Zadanie 2e
Napisz test sparametryzowany plikiem songs.csv.
```java
    @ParameterizedTest
    @CsvFileSource(resources = "/songs.csv", numLinesToSkip = 1)
    void readFromCsv(int id, String artist, String title, int length) throws SQLException {
        Optional<Song> maybe = Song.Persistence.read(id);
        Assert.assertTrue(maybe.isPresent());

        String artistDB = maybe.get().artist();
        String titleDB = maybe.get().title();
        int lengthDB = maybe.get().durationSeconds();

        Assert.assertEquals(artist, artistDB);
        Assert.assertEquals(title, titleDB);
        Assert.assertEquals(length, lengthDB);
    }
```
`@CsvFileSource(resources = "/songs.csv", numLinesToSkip = 1)` zapewniamy piosenki do testów z pliku `songs.csv`, pomijamy pierwszy wiersz bo to nagłówek. Reszta bardzo podobna do poprzesniego testu.
Zadanie 3a.
Zapoznaj się z klasą ListenerAccount. Klasa rozszerza klasę Account o liczbę kredytów oraz listę piosenek posiadaną na koncie. Obie dane znajdują się wyłącznie w bazie danych. Napisz test sprawdzający poprawność rejestracji nowego konta.
```java
    @Test
    void registerCheck() {

        int userId = register("userTest", "passwordTest");

        Assert.assertTrue(userId > 0);
    }
```
za pomocą `INSERT(...)` dodajemy do bazy nowego użytkownika i od userId dostajemy numer kolumny, następnie sprawdzamy czy `userId` jest większe `0`, jest to prawda, znaczy to że rejestracja użytkownika się powiodła - poprawny numer kolumny.

Zadanie 3b.
Napisz test sprawdzający poprawne logowanie się do konta.
```java
    @Test
    void loginCheck() throws AuthenticationException, SQLException {
        AccountManager ac = new AccountManager();
        int userId = register("userTest", "passwordTest");

        boolean auth = ac.authenticate("userTest", "passwordTest");
        Assert.assertTrue(auth);
    }
```
Rejestrujemy nowego użytkownika, `auth` zwraca `true` jeśli hasło się zgadza.


### Przydatne linki: 
https://www.freecodecamp.org/news/java-unit-testing/


https://www.youtube.com/watch?v=zcRrrZUABkA
