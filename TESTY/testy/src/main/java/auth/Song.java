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
