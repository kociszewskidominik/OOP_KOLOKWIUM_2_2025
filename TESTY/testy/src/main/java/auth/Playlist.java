package auth;

import java.util.ArrayList;

public class Playlist extends ArrayList<Song> {
    //1f 1g 1h
    public Song atSecond(int seconds){
        if(seconds < 0) {
            throw new IndexOutOfBoundsException("Ujemny czas!");
        }

        int totalSeconds = 0;
        for(Song s : this){
            totalSeconds += s.durationSeconds();
        }
        if(seconds >= totalSeconds) throw new IndexOutOfBoundsException("Podany czas nie mieści się!");

        int sum = 0;
        for(Song s : this) {
            sum += s.durationSeconds();
            if(seconds < sum) {
                return s;
            }
        }

        return null;
    }
}
