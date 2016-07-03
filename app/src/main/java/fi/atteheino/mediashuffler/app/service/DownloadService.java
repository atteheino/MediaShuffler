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
package fi.atteheino.mediashuffler.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import fi.atteheino.mediashuffler.app.Constants;
import fi.atteheino.mediashuffler.app.Options;
import fi.atteheino.mediashuffler.app.R;
import fi.atteheino.mediashuffler.app.SerializableMusicTrack;
import fi.atteheino.mediashuffler.app.activity.ResultActivity;
import fi.atteheino.mediashuffler.app.activity.ShuffleActivity;
import fi.atteheino.mediashuffler.app.utils.Downloader;

/**
 * Created by Atte on 16.12.2014.
 */
public class DownloadService extends IntentService {
    public static final String STATUS_RUNNING = "DownloadService.running";
    public static final String STATUS_STOPPED = "DownloadService.stopped";
    public static final String STATUS_IDLE = "DownloadService.idle";
    private static final String TAG = "DownloadService";
    private static final int NOTIFICATION_ID = 123;
    private String status;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadService() {
        super("DownloadService");
        this.status = STATUS_IDLE;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Options options = (Options) intent.getSerializableExtra("options");
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_download_in_progress))
                .setSmallIcon(R.drawable.ic_launcher_ic_launcher)
                .setAutoCancel(true);
        Intent shuffleIntent = new Intent(this, ShuffleActivity.class);
        shuffleIntent.putExtra("Options", options);
        shuffleIntent.putExtra("STATUS_RUNNING", true);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ShuffleActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(shuffleIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        handleDownload(options);
    }

    private void handleDownload(Options options) {
        this.status = STATUS_RUNNING;
        final List<SerializableMusicTrack> randomMusicTrackList = options.getMusicTrackList();
        int counter = 0;
        for (SerializableMusicTrack track : randomMusicTrackList) {
            Downloader.downloadFile(track.getURL(),
                    options.getTargetFolderName(),
                    track.getFullFilename());
            counter++;
            final int progress = (int) (((float) counter / randomMusicTrackList.size()) * 100);
            //publish progress to the App
            publishProgress(progress);
            //then publish progress to Notification
            mBuilder.setProgress(100, progress, false);
            mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

        }
        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra("Options", options);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ResultActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setContentText("Download complete")
                // Removes the progress bar
                .setProgress(0, 0, false);
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

        this.status = STATUS_IDLE;
    }

    private void publishProgress(int i) {
        /*
     * Creates a new Intent containing a Uri object
     * BROADCAST_ACTION is a custom Intent action
     */
        Intent localIntent =
                new Intent(Constants.BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(Constants.EXTENDED_DATA_STATUS, i);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
