package auth;

public record Account(int id, String username) {
    public Account(int id, String username) {
        this.id = id;
        this.username = username;
    }
}
