package org.example;

import database.DatabaseConnection;
import auth.Account;
import auth.AccountManager;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection.connect("songs.db");

            AccountManager.initSchema();

            AccountManager mgr = new AccountManager();

            mgr.register("domingo", "dupa123");

            boolean ok = mgr.authenticate("domingo", "dupa123");
            System.out.println("Authenticated? " + ok);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect();
        }
    }
}
