# Main
```java
package org.example;

import auth.AccountManager;
import database.DatabaseConnection;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws SQLException {
        //tworzenie tabeli
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");

        Connection con = db.getConnection();
        Statement stm = con.createStatement();

        //String sql = "CREATE TABLE IF NOT EXISTS accounts(id INTEGER PRIMARY KEY, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL);";
        //stm.executeUpdate(sql);


        AccountManager accountManager = new AccountManager();
        //ccountManager.register("domingo", "dupa123");
        accountManager.authenticate("domingo", "dupa123");
        accountManager.getAccount("domingo");

        System.out.println(Paths.get("test.db").toAbsolutePath());
        stm.close();
        db.disconnect();
    }
}
```
# Klasa DatabaseConnection
```java
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private java.sql.Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void connect(String url) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + url);
    }

    public void disconnect() throws SQLException {
        if(this.connection != null && !this.connection.isClosed())
            this.connection.close();

        this.connection = null;
    }
}
```
Deklaracja prywatnego pola `connection`, w którym będzie przechowywane aktywne połączenie do bazy.
```java
    private java.sql.Connection connection;
```
Udostępnienie obiektu `Connection` poza klasę, by inne części aplikacji miały dostęp do obiektu do tworzenia zapytań jak `prepareStatement` lub `createStatement`.
```java
    public Connection getConnection() {
        return connection;
    }
```
Metoda `connect` służy do połaczęnia się z bazą, przyjmuję ścieżkę do bazy `path`. `= DriveManager.getConnection(...)` otwiera połaczenie z bazą, jeśli taka nie istnieje stworzy ją.
```java
    public void connect(String url) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + url);
    }
```
Metoda `disconnect` zamyka połączenie z bazą jeśli nie jest puste lub już wcześniej zamknięte. Na koniec ustawiamy `connection` na `null`, bo obiekt klasy nadal istnieje w pamięci.
```java
    public void disconnect() throws SQLException {
        if(this.connection != null && !this.connection.isClosed())
            this.connection.close();

        this.connection = null;
    }
```
# Klasa AccountManager
```java
package auth;

import database.DatabaseConnection;

import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

public class AccountManager {
    public void register(String username, String password) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Statement stmt = conn.createStatement();

        String insertSQL = "INSERT INTO accounts (name, password) VALUES ('" + username + "', '" + hashedPassword + "');";

        stmt.executeUpdate(insertSQL);

        db.disconnect();
    }

    public boolean authenticate(String username, String password) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String sqlQuery = "SELECT password FROM accounts WHERE name = ?";

        PreparedStatement stmt = conn.prepareStatement(sqlQuery);
        stmt.setString(1, username);

        ResultSet rslt = stmt.executeQuery();

        if(rslt.next()){
            String hashedPassword = rslt.getString("password");
            if(BCrypt.checkpw(password, hashedPassword)){
                rslt.close();
                stmt.close();
                db.disconnect();

                return true;
            } else {
                rslt.close();
                stmt.close();
                db.disconnect();

                return false;
            }
        } else {
            rslt.close();
            stmt.close();
            db.disconnect();

            return false;
        }
    }

    public Account getAccount(String username) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String sqlSelect = "SELECT id, name FROM accounts where name = ?";

        PreparedStatement stmt = conn.prepareStatement(sqlSelect);
        stmt.setString(1, username);

        ResultSet rslt = stmt.executeQuery();

        if(rslt.next()){
            int id = rslt.getInt("id");
            String usrnm = rslt.getString("name");

            rslt.close();
            stmt.close();
            db.disconnect();

            return new Account(id, usrnm);
        } else {
            throw new SQLException("Brak konta.");
        }
    }
}

```
Metoda `register` przyjmuję nazwę i hasło podane przez użytkownika.
```java
 public void register(String username, String password) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Statement stmt = conn.createStatement();

        String insertSQL = "INSERT INTO accounts (name, password) VALUES ('" + username + "', '" + hashedPassword + "');";

        stmt.executeUpdate(insertSQL);

        db.disconnect();
    }
```
Tworzymy obiekt, otwieramy plik bazy `test.db` i łączymy się z nią.

```java
    DatabaseConnection db = new DatabaseConnection();
    db.connect("test.db");
    Connection conn = db.getConnection();
```
Hashujemy podane przez użytkownika jawne hasło.
```java
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
```
Tworzymy obiekt `Statement`, który pozwoli nam na wysłanie zapytania `insertSQL` do tabeli. 


`stmt.executeUpdate(insertSQL);` wykonuje to zapytanie.


Na koniec zamykamy połączenie z bazą `db.disconnect();`.

Metoda `authenticate` sprawdza czy podana przez użytkownika nazwa i hasła zgadzają się z tymi w bazie i zwraca zależnie od tego `true` / `false`.
```java
    public boolean authenticate(String username, String password) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String sqlQuery = "SELECT password FROM accounts WHERE name = ?";

        PreparedStatement stmt = conn.prepareStatement(sqlQuery);
        stmt.setString(1, username);

        ResultSet rslt = stmt.executeQuery();

        if(rslt.next()){
            String hashedPassword = rslt.getString("password");
            if(BCrypt.checkpw(password, hashedPassword)){
                rslt.close();
                stmt.close();
                db.disconnect();

                return true;
            } else {
                rslt.close();
                stmt.close();
                db.disconnect();

                return false;
            }
        } else {
            rslt.close();
            stmt.close();
            db.disconnect();

            return false;
        }
    }
```
Ponownie tworzymy obiekt, zapytanie do bazy i połączenie z bazą.
```java
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();
```
Tworzymy przygotowane zapytanie, w celu uniknięcia złośliwych danych ręcznych.
```java
        PreparedStatement stmt = conn.prepareStatement(sqlQuery);
        stmt.setString(1, username);

        ResultSet rslt = stmt.executeQuery();
```
Jeśli znaleźliśmy kolejny rekord z podaną nazwą użytkownika, pobieramy zashawone hasło `hashedPassaword`. Jeśli hasło podane przez użytkownika pasuje do hasła pobranego z bazy `BCrypt.checkpw(password, hashedPassword)`, zwracamy `true` i zamykamy połączenia, jeśli nie pasują zwracamy `false`, gdy nie znajdziemy kolejnego rekordu również zwracamy `false` i zamykamy połączenie. 
```java
        if(rslt.next()){
            String hashedPassword = rslt.getString("password");
            if(BCrypt.checkpw(password, hashedPassword)){
                rslt.close();
                stmt.close();
                db.disconnect();

                return true;
            } else {
                rslt.close();
                stmt.close();
                db.disconnect();

                return false;
            }
        } else {
            rslt.close();
            stmt.close();
            db.disconnect();

            return false;
        }
```
Metoda `getAccount` zwraca nam nowy obiekt `Account` z pobranymi wartościami z tabeli.
```java
    public Account getAccount(String username) throws SQLException {
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String sqlSelect = "SELECT id, name FROM accounts where name = ?";

        PreparedStatement stmt = conn.prepareStatement(sqlSelect);
        stmt.setString(1, username);

        ResultSet rslt = stmt.executeQuery();

        if(rslt.next()){
            int id = rslt.getInt("id");
            String usrnm = rslt.getString("name");

            rslt.close();
            stmt.close();
            db.disconnect();

            return new Account(id, usrnm);
        } else {
            throw new SQLException("Brak konta.");
        }
    }
```
Nic się nie zmienia, tworzymy połączenie z bazą, zapytanie do bazy itd.
```java
        DatabaseConnection db = new DatabaseConnection();
        db.connect("test.db");
        Connection conn = db.getConnection();

        String sqlSelect = "SELECT id, name FROM accounts where name = ?";

        PreparedStatement stmt = conn.prepareStatement(sqlSelect);
        stmt.setString(1, username);

        ResultSet rslt = stmt.executeQuery();
```
Jeśli znajdziemy kolejny rekord z podaną nazwą przez użytkownika, pobieramy za pomocą `int id = rslt.getInt("id");` i `String usrnm = rslt.getString("name");` id oraz nazwę użytkownika z bazy, zamykamy połączenia i zwracamy nowy obiekt `Account`, jeśli nie znajdziemy rekordu zwracamy `SQLException(...)`.
```java
        if(rslt.next()){
            int id = rslt.getInt("id");
            String usrnm = rslt.getString("name");

            rslt.close();
            stmt.close();
            db.disconnect();

            return new Account(id, usrnm);
        } else {
            throw new SQLException("Brak konta.");
        }
```
# Klasa Account
```java
package auth;

public record Account(int id, String username) {
    public Account(int id, String username) {
        this.id = id;
        this.username = username;
    }
}
```
