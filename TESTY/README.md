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
### Zadanie 1b
### Zadanie 1c
### Zadanie 1d
### Zadanie 1e
### Zadanie 1f
### Zadanie 1g
### Zadanie 1h
