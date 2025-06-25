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
