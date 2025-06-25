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