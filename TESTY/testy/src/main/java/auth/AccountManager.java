package auth;

import database.DatabaseConnection;
import auth.Account;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * Zarządza kontami w zewnętrznej tabeli `accounts`.
 * Korzysta z zewnętrznej klasy DatabaseConnection (statycznej).
 */
public class AccountManager {

    /**
     * Tworzy schemat tabeli `accounts`:
     * id INTEGER PRIMARY AUTOINCREMENT,
     * name TEXT UNIQUE NOT NULL,
     * password TEXT NOT NULL
     */
    public static void initSchema() throws SQLException {
        String ddl = """
            CREATE TABLE IF NOT EXISTS accounts (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT    UNIQUE NOT NULL,
              password TEXT NOT NULL
            );
            """;
        try (Statement st = DatabaseConnection.getConnection().createStatement()) {
            st.executeUpdate(ddl);
        }
    }

    /**
     * Rejestruje nowe konto (hashuje hasło i wrzuca do bazy).
     */
    public void register(String username, String password) throws SQLException {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql  = "INSERT INTO accounts(name,password) VALUES(?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
        }
    }

    /**
     * Sprawdza login i hasło; zwraca true, jeśli poprawne.
     */
    public boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT password FROM accounts WHERE name = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString("password");
                return BCrypt.checkpw(password, hash);
            }
        }
    }

    /**
     * Pobiera konto o danym loginie; jeśli nie istnieje, rzuca SQLException.
     */
    public Account getAccount(String username) throws SQLException {
        String sql = "SELECT id,name FROM accounts WHERE name = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Brak konta o loginie: " + username);
                }
                return new Account(rs.getInt("id"), rs.getString("name"));
            }
        }
    }
}
