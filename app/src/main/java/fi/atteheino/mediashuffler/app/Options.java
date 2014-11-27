package fi.atteheino.mediashuffler.app;

import org.teleal.cling.support.model.item.MusicTrack;

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
    private List<MusicTrack> musicTrackList;
    private int targetSizeMegaBytes;

    public int getTargetSizeMegaBytes() {
        return targetSizeMegaBytes;
    }

    public void setTargetSizeMegaBytes(int targetSizeMegaBytes) {
        this.targetSizeMegaBytes = targetSizeMegaBytes;
    }

    public List<MusicTrack> getMusicTrackList() {
        return musicTrackList;
    }

    public void setMusicTrackList(List<MusicTrack> musicTrackList) {
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
