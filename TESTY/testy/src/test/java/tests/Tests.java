package tests;

import auth.*;
import database.DatabaseConnection;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.testng.Assert;

import javax.naming.AuthenticationException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import static auth.Account.Persistence.authenticate;
import static auth.Account.Persistence.register;


public class Tests {
    @BeforeTest
    public static void connect(){
        DatabaseConnection.connect("song.db", "supercool");
    }

    @AfterTest
    public static void disconnect(){
        DatabaseConnection.disconnect("supercool");

    }

    //1b
    @Test
    public void isPlaylistEmpty(){
        Playlist playlist = new Playlist();

        Assert.assertTrue(playlist.isEmpty());
    }

    //1c
    @Test
    public void isPlaylistSizeOne() {
        Playlist playlist = new Playlist();
        Song song = new Song(1,"ArtistX", "TitleY", 123);
        playlist.add(song);

        Assert.assertTrue(playlist.size() == 1);
    }

    //1d i 1e
    @Test
    public void sameSongInPlaylist() {
        Playlist playlist = new Playlist();
        Song song = new Song(2, "ArtistX", "TitleY", 123);
        playlist.add(song);

        for(Song s : playlist) {
            Assert.assertTrue(s == song);
        }
    }

    //1f
    @Test
    public void chosenSong() {
        Playlist playlist = new Playlist();

        playlist.add(new Song(1,"ABC", "XYZ", 20));
        playlist.add(new Song(2,"DEF", "YXZ", 19));
        playlist.add(new Song(3,"GHI", "ZYX", 35));
        playlist.add(new Song(4,"JKL", "XZY", 71));

        Assert.assertTrue(playlist.atSecond(41) == playlist.get(3));
    }

    // 1g i 1h
    @Test
    public void negativeSecondsException(){
        Playlist playlist = new Playlist();

        Assert.assertEquals("Ujemny czas!", playlist.atSecond(-15));
    }

    @Test
    public void secondsOutOfPlaylistException(){
        Playlist playlist = new Playlist();

        Assert.assertEquals("Podany czas nie mieści się!", playlist.atSecond(2137));
    }

    // 2b
    @Test
    public  void readSongWihExistingIndex() throws SQLException {
        Playlist playlist = new Playlist();
        playlist.add(new Song(8, "Young Mlody", "what", 69));

        Assert.assertEquals(playlist.get(8), Song.Persistence.read(8));
    }

    // 2c
    @Test
    public  void readSongWihNoExistingIndex() throws SQLException {
        Playlist playlist = new Playlist();
        playlist.add(new Song(8, "Young Mlody", "what", 69));

        Assert.assertEquals(playlist.get(9), Song.Persistence.read(8));
    }

    // 2d
    // test sparametryzowany oznacza że jedna metoda testowa zostanie wykonana wielokrotnie, każdorazowo z innymi danymi wejściowym i oczekiwanym wynikiem
    @ParameterizedTest
    @MethodSource("provideForTesting")
    /* wcześniejsza zła metoda, porównujemy int z obiektem co zawsze zwróci fałsz
    void indexStream(int index, Song expectedSong) {
        Assert.assertEquals(index, expectedSong);
    }
    */

    //poprawiona metoda, pobieramy piosenke względem indeksu i porównujemy
    void indexStream(int index, Song expectedSong) throws SQLException {
        Optional<Song> maybe = Song.Persistence.read(index);
        Assert.assertTrue(maybe.isPresent()); //sprawdzamy czy piosenka o danym indeksie w ogóle istnieje

        Song song = maybe.get();

        Assert.assertEquals(expectedSong, song);
    }
    static Stream<Arguments> provideForTesting() {
        return Stream.of(
                Arguments.of(1, new Song(1, "Artist1", "Title1", 10)),
                Arguments.of(2, new Song(2, "Artist2", "Title2", 20)),
                Arguments.of(3, new Song(3, "Artist3", "Title3", 30)),
                Arguments.of(4, new Song(4, "Artist4", "Title4", 40))
        );
    }

    // 2e
    @ParameterizedTest
    @CsvFileSource(resources = "/songs.csv", numLinesToSkip = 1)
    void readFromCsv(int id, String artist, String title, int length) throws SQLException {
        Optional<Song> maybe = Song.Persistence.read(id);
        Assert.assertTrue(maybe.isPresent());

        String artistDB = maybe.get().artist();
        String titleDB = maybe.get().title();
        int lengthDB = maybe.get().durationSeconds();

        Assert.assertEquals(artist, artistDB);
        Assert.assertEquals(title, titleDB);
        Assert.assertEquals(length, lengthDB);
    }

    // 3a
    @Test
    void registerCheck() {
        // za pomocą INSERT(...) dodajemy do bazy nowego użytkownika i do userId dostajemy numer kolumny
        int userId = register("userTest", "passwordTest");

        Assert.assertTrue( userId > 0);
    }

    // 3b
    @Test
    void loginCheck() throws AuthenticationException, SQLException {
        AccountManager ac = new AccountManager();
        int userId = register("userTest", "passwordTest");

        boolean auth = ac.authenticate("userTest", "passwordTest");
        Assert.assertTrue(auth);
    }


}
