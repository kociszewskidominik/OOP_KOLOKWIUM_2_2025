package auth;

import database.DatabaseConnection;
import javax.naming.AuthenticationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ListenerAccount extends Account {
    public ListenerAccount(int id, String username) {
        super(id, username);
    }

    public Playlist createPlaylist(List<Integer> songIds) throws SQLException {
        Playlist playlist = new Playlist();
        int accountId = getId();

        for (Integer songId : songIds) {
            if (!Persistence.hasSong(accountId, songId)) {
                Persistence.addSong(accountId, songId);
            }

            Optional<Song> optionalSong = Song.Persistence.read(songId);
            if (optionalSong.isPresent()) {
                playlist.add(optionalSong.get());
            } else {
                throw new SQLException("Song o ID " + songId + " nie istnieje");
            }
        }

        return playlist;
    }

    public static class Persistence {
        public static void init() throws SQLException {
            Account.Persistence.init();
            {
                String sql = """
                    CREATE TABLE IF NOT EXISTS listener_account(
                      id_account INTEGER NOT NULL PRIMARY KEY,
                      credits    INTEGER NOT NULL
                    );
                    """;
                try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
            {
                String sql = """
                    CREATE TABLE IF NOT EXISTS owned_songs(
                      id_account INTEGER NOT NULL,
                      id_song    INTEGER NOT NULL,
                      PRIMARY KEY(id_account, id_song)
                    );
                    """;
                try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
        }

        public static int register(String username, String password) throws SQLException {
            int id = Account.Persistence.register(username, password);
            String sql = "INSERT INTO listener_account(id_account, credits) VALUES(?, 0)";
            try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            return id;
        }

        public static void addSong(int accountId, int songId) throws SQLException {
            String sql = "INSERT INTO owned_songs(id_account, id_song) VALUES(?, ?)";
            try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setInt(2, songId);
                stmt.executeUpdate();
            }
        }

        public static boolean hasSong(int accountId, int songId) throws SQLException {
            String sql = "SELECT 1 FROM owned_songs WHERE id_account = ? AND id_song = ?";
            try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, accountId);
                stmt.setInt(2, songId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        }

        public static ListenerAccount authenticate(String username, String password)
                throws AuthenticationException {
            Account acct = Account.Persistence.authenticate(username, password);
            return new ListenerAccount(acct.getId(), acct.getUsername());
        }
    }
}
