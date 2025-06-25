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
