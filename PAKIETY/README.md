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
