/*
 * Copyright 2015 Atte Heino
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
                .append(getFirstArtist())
                .append(" - ")
                .append(getAlbum())
                .append(" - ")
                .append(getTitle())
                .append(getExtension());
        final String filename = stringBuilder.toString().replace("/", "").replace("?", "");
        Log.d(TAG, "filename: " + filename);
        return filename;
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
