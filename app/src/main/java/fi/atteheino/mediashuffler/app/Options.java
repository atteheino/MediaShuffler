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

import java.io.Serializable;
import java.util.List;

/**
 * Created by Atte on 7.6.2014.
 */
public class Options implements Serializable {

    private String DLNADevice;
    private String DLNADeviceUDN;
    private String sourceFolderID;
    private String sourceFolderName;
    private String targetFolderName;
    private List<SerializableMusicTrack> musicTrackList;
    private int targetSizeMegaBytes;

    public int getTargetSizeMegaBytes() {
        return targetSizeMegaBytes;
    }

    public void setTargetSizeMegaBytes(int targetSizeMegaBytes) {
        this.targetSizeMegaBytes = targetSizeMegaBytes;
    }

    public List<SerializableMusicTrack> getMusicTrackList() {
        return musicTrackList;
    }

    public void setMusicTrackList(List<SerializableMusicTrack> musicTrackList) {
        this.musicTrackList = musicTrackList;
    }

    public String getTargetFolderName() {
        return targetFolderName;
    }

    public void setTargetFolderName(String targetFolderName) {
        this.targetFolderName = targetFolderName;
    }

    public String getSourceFolderName() {
        return sourceFolderName;
    }

    public void setSourceFolderName(String sourceFolderName) {
        this.sourceFolderName = sourceFolderName;
    }

    public String getSourceFolderID() {
        return sourceFolderID;
    }

    public void setSourceFolderID(String sourceFolderID) {
        this.sourceFolderID = sourceFolderID;
    }

    public String getDLNADevice() {
        return DLNADevice;
    }

    public void setDLNADevice(String DLNADevice) {
        this.DLNADevice = DLNADevice;
    }

    public String getDLNADeviceUDN() {
        return DLNADeviceUDN;
    }

    public void setDLNADeviceUDN(String DLNADeviceUDN) {
        this.DLNADeviceUDN = DLNADeviceUDN;
    }
}
