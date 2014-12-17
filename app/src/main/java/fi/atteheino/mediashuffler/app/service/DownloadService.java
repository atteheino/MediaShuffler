package fi.atteheino.mediashuffler.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.teleal.cling.support.model.item.MusicTrack;

import java.util.List;

import fi.atteheino.mediashuffler.app.Options;
import fi.atteheino.mediashuffler.app.utils.Downloader;

/**
 * Created by Atte on 16.12.2014.
 */
public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";
    public static final String STATUS_RUNNING = "DownloadService.running";
    public static final String STATUS_STOPPED = "DownloadService.stopped";
    public static final String STATUS_IDLE = "DownloadService.idle";
    private String status;


    public DownloadService() {
        super("DownloadService");
        this.status = STATUS_IDLE;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Options options = (Options) intent.getSerializableExtra("options");
        handleDownload(options);
    }

    private void handleDownload(Options options) {
        this.status = STATUS_RUNNING;
        final List<MusicTrack> randomMusicTrackList = options.getMusicTrackList();
        int counter = 0;
        for (MusicTrack track : randomMusicTrackList) {
            Downloader.downloadFile(track.getFirstResource().getValue(),
                    options.getTargetFolderName(),
                    getFilename(track));
            counter++;
            //publishProgress((counter / randomMusicTrackList.size()) * 100);

        }
    }

    private String getFilename(MusicTrack track) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(track.getOriginalTrackNumber())
                .append(" - ")
                .append(track.getFirstArtist().toString().replace("/", ""))
                .append(" - ")
                .append(track.getAlbum())
                .append(" - ")
                .append(track.getTitle())
                .append(getExtension(track));
        Log.d(TAG, "filename: " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String getExtension(MusicTrack track) {

        final String mimetype = track.getFirstResource().getProtocolInfo().getContentFormatMimeType().toString();
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
}
