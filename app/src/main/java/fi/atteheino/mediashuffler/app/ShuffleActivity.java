package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Vector;

import fi.atteheino.mediashuffler.app.service.DownloadService;


public class ShuffleActivity extends Activity {

    AndroidUpnpService upnpService;
    private Options options;
    private ProgressBar mProgressBar;
    private Vector<MusicTrack> musicTracks = new Vector<MusicTrack>();
    private static final String TAG = "ShuffleActivity";
    private Intent mDownloadServiceIntent;


    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected called from thread " + Thread.currentThread().getId());
            upnpService = (AndroidUpnpService) service;
            findCorrectDevice(upnpService.getRegistry().getRemoteDevices());
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService.getRegistry().removeAllLocalDevices();
            upnpService.getRegistry().removeAllRemoteDevices();
            upnpService = null;

        }
    };

    private void findCorrectDevice(Collection<RemoteDevice> remoteDevices) {
        Log.d(TAG, "findCorrectDevice called from thread " + Thread.currentThread().getId());
        for (RemoteDevice remoteDevice : remoteDevices) {
            if (remoteDevice.getIdentity().getUdn().getIdentifierString().equals(options.getDLNADeviceUDN())) {
                RemoteService[] services = remoteDevice.getRoot().findServices();
                for (RemoteService remoteService : services) {
                    if (remoteService.getServiceType().getType().equals("ContentDirectory")) {
                        DLNADirectoryBrowser dlnaDirectoryBrowser = new DLNADirectoryBrowser(remoteService, upnpService, myCallbackObserver);
                        dlnaDirectoryBrowser.browseThruFoldersAndGetFileUris(options.getSourceFolderID());
                    }
                }
            }
        }
    }


    private Observer myCallbackObserver = new Observer() {
        int countOfBrowsers = 0;
        @Override
        public void update(Observable observable, Object o) {
            synchronized (this) {
                // If there is no parameter passed, then the browse method has been called. In this case
                // we shall add counter value.
                // In other case, we will decrease counter value and add values to map
                if (o == null) {
                    countOfBrowsers++;
                } else {
                    List<MusicTrack> tempUris = (List<MusicTrack>) o;
                    if (tempUris != null && tempUris.size() != 0) {
                        musicTracks.addAll(tempUris);
                    }
                    countOfBrowsers--;
                }
                // Now we are waiting for the situation where countOfBrowsers is "0".
                // When this occurs, we know that all URI's have been gathered and we can continue with the processing.
                if (countOfBrowsers == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            transferFiles();
                        }
                    });

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuffle);

        getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        // Get the options needed
        options = (Options) getIntent().getSerializableExtra("Options");

        mProgressBar = (ProgressBar) findViewById((R.id.progressBar));
        mProgressBar.setIndeterminate(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called from thread " + Thread.currentThread().getId());
    }

    public void transferFiles() {
        Log.d(TAG, "transferFiles called from thread " + Thread.currentThread().getId());
        //Let's stop the progressbar and start feeding it correct values.
        if (mDownloadServiceIntent == null) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(0);
            mProgressBar.invalidate();
            ((TextView) findViewById(R.id.statusText)).setText(R.string.shuffle_status_moving_files);
            options.setMusicTrackList(generateRandomList());
            mDownloadServiceIntent = new Intent(this, DownloadService.class);
            mDownloadServiceIntent.putExtra("options", options);
            startService(mDownloadServiceIntent);
        } else {
            Log.d(TAG, "DownloadService is already created and running.");
        }

        //TODO: Should I backup the previous collection?
        //TODO: Should the old files be removed or only add new files? (Add this as feature to be implemented in the future)
    }

    private List<MusicTrack> generateRandomList() {

        List<MusicTrack> randomMusicTrackList = new ArrayList<MusicTrack>();
        final long targetSizeMegaBytes = options.getTargetSizeMegaBytes();
        final long MEGABYTE = 1024L * 1024L;
        long currentSizeMegaBytes = 0;
        Random randomizer = new Random();
        /**
         * Let's check that the collection we have collected is smaller than the maximum set by the user
         * and that there is still space left for some song (5 megabytes).
         * We must also check that there are songs left to add.
         */
        while (currentSizeMegaBytes < targetSizeMegaBytes
                && targetSizeMegaBytes - currentSizeMegaBytes > 5L
                && randomMusicTrackList.size() < musicTracks.size()) {
            int randomSongIndex = randomizer.nextInt(musicTracks.size());
            if (musicTracks.get(randomSongIndex) != null
                    || musicTracks.get(randomSongIndex).getFirstResource().getSize() != null) {
                if ((musicTracks.get(randomSongIndex).getFirstResource().getSize() / MEGABYTE) + currentSizeMegaBytes < targetSizeMegaBytes) {
                    //Randomizer could hand the same value twice
                    if (!randomMusicTrackList.contains(musicTracks.get(randomSongIndex))) {
                        randomMusicTrackList.add(musicTracks.get(randomSongIndex));
                        currentSizeMegaBytes += musicTracks.get(randomSongIndex).getFirstResource().getSize() / MEGABYTE;
                    }
                } else {
                    // Collection if full, better to return it as fast as possible.
                    return randomMusicTrackList;
                }
            }
        }
        return randomMusicTrackList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shuffle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCancelAdd() {
        if (mShuffleTask != null && mShuffleTask.getStatus() == AsyncTask.Status.RUNNING) {
            mShuffleTask.cancel(true);
            mShuffleTask = null;
        }
    }


    /*private class ShuffleFilesTask extends AsyncTask<Options, Integer, String> {
        private static final String TAG = "ShuffleFilesTask";
        @Override
        protected String doInBackground(Options... optionses) {
            final List<MusicTrack> randomMusicTrackList = optionses[0].getMusicTrackList();
            int counter = 0;
            for (MusicTrack track : randomMusicTrackList) {
                Downloader.downloadFile(track.getFirstResource().getValue(),
                        optionses[0].getTargetFolderName(),
                        getFilename(track));
                counter++;
                publishProgress((counter / randomMusicTrackList.size()) * 100);
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return "Done";
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

        @Override
        protected void onPostExecute(String s) {
            ((TextView)findViewById(R.id.statusText)).setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), "Shuffle task ready", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            mProgressBar.setProgress(values[0]);
            Log.d(TAG, "onProgressUpdate called with value: " + values[0]);
        }
    }*/
}
