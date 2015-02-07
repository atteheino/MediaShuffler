package fi.atteheino.mediashuffler.app;


import android.util.Log;

import java.io.Serializable;

/**
 * Created by Atte on 19.12.2014.
 */
public class SerializableMusicTrack implements Serializable {
    private static final String TAG = "SerializableMusicTrack";
    private String URL;
    private int originalTrackNumber;
    private String firstArtist;
    private String album;
    private String title;
    private String mimetype;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public int getOriginalTrackNumber() {
        return originalTrackNumber;
    }

    public void setOriginalTrackNumber(int originalTrackNumber) {
        this.originalTrackNumber = originalTrackNumber;
    }

    public String getFirstArtist() {
        return firstArtist;
    }

    public void setFirstArtist(String firstArtist) {
        this.firstArtist = firstArtist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getFullFilename() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getOriginalTrackNumber())
                .append(" - ")
                .append(getFirstArtist().replace("/", ""))
                .append(" - ")
                .append(getAlbum().replace("/", ""))
                .append(" - ")
                .append(getTitle().replace("/", ""))
                .append(getExtension());
        Log.d(TAG, "filename: " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String getExtension() {

        final String mimetype = getMimetype();
        if (mimetype.equalsIgnoreCase("audio/mpeg")) {
            return ".mp3";
        } else if (mimetype.equalsIgnoreCase("audio/ogg")) {
            return ".ogg";
        } else if (mimetype.equalsIgnoreCase("audio/mp4")) {
            return ".mp4";
        } else if (mimetype.equalsIgnoreCase("audio/vnd.wav")) {
            return ".wav";
        } else return ".mp3";
    }

    @Override
    public String toString() {
        return getFirstArtist() + " :: " + getAlbum() + " :: " + getTitle();
    }
}
