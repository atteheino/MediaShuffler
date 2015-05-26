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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.message.header.UDNHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
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

    static final String SERVICE_STATUS = "serviceStatus";
    static final String FILELIST_STATUS = "filelistStatus";

    AndroidUpnpService upnpService;
    private BasicRegistryListener mListener = new BasicRegistryListener();

    private Options options;
    private ProgressBar mProgressBar;
    private Vector<MusicTrack> musicTracks = new Vector<MusicTrack>();
    private static final String TAG = "ShuffleActivity";
    private boolean mServiceIsRunning;
    private boolean mFileListGenerated;
    private DownloadStateReceiver mDownloadStateReceiver;


    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected called from thread " + Thread.currentThread().getId());
            //Only browse the registry if we have not populated the filelists yet.
            if (mFileListGenerated != true) {
                upnpService = (AndroidUpnpService) service;
                upnpService.getRegistry().addListener(mListener);
                if (upnpService.getRegistry().getRemoteDevices().size() == 0 && options.getDLNADeviceUDN() != null) {
                    upnpService.getControlPoint().search(new UDNHeader(new UDN(options.getDLNADeviceUDN())));
                }
                findCorrectDevice(upnpService.getRegistry().getRemoteDevices());
            }
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
                            // Set the boolean so that we do not populate the filelist ever again.
                            mFileListGenerated = true;
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

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            mFileListGenerated = savedInstanceState.getBoolean(FILELIST_STATUS);
            mServiceIsRunning = savedInstanceState.getBoolean(SERVICE_STATUS);
        }

        // Get the options needed
        options = (Options) getIntent().getSerializableExtra("Options");
        //Find the progress bar to update.
        mProgressBar = (ProgressBar) findViewById((R.id.progressBar));

        //Check if DownloadService is already running and we are getting here from Notification.
        boolean downloadInProgress = getIntent().getBooleanExtra("STATUS_RUNNING", false);
        if (!downloadInProgress) {
            mProgressBar.setIndeterminate(true);
        } else {
            mProgressBar.setIndeterminate(false);
            ((TextView) findViewById(R.id.statusText)).setText(R.string.shuffle_status_moving_files);
            mFileListGenerated = true;
        }

        getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        /*
    * Creates an intent filter for DownloadStateReceiver that intercepts broadcast Intents
    */
        registerBroadcastReceivers();
        Log.d(TAG, "oCreate called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SERVICE_STATUS, mServiceIsRunning);
        outState.putBoolean(FILELIST_STATUS, mFileListGenerated);
        super.onSaveInstanceState(outState);
    }

    /*
        * Creates an intent filter for DownloadStateReceiver that intercepts broadcast Intents
        */
    private void registerBroadcastReceivers() {

        // The filter's action is BROADCAST_ACTION
        IntentFilter mStatusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);
        // Sets the filter's category to DEFAULT
        mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        // Instantiates a new DownloadStateReceiver
        mDownloadStateReceiver = new DownloadStateReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStateReceiver,
                mStatusIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterBroadcastReceivers();
        Log.d(TAG, "onPause called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceivers();
        Log.d(TAG, "onResume called from thread " + Thread.currentThread().getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceivers();


        Log.d(TAG, "onDestroy called from thread " + Thread.currentThread().getId());
    }

    private void unRegisterBroadcastReceivers() {
        // If the DownloadStateReceiver still exists, unregister it and set it to null
        if (mDownloadStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadStateReceiver);
            mDownloadStateReceiver = null;
        }
    }

    public void transferFiles() {
        Log.d(TAG, "transferFiles called from thread " + Thread.currentThread().getId());
        //Let's stop the progressbar and start feeding it correct values.
        if (mServiceIsRunning == false) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(0);
            mProgressBar.invalidate();
            ((TextView) findViewById(R.id.statusText)).setText(R.string.shuffle_status_moving_files);
            options.setMusicTrackList(generateRandomList());
            Intent downloadServiceIntent = new Intent(this, DownloadService.class);
            downloadServiceIntent.putExtra("options", options);
            startService(downloadServiceIntent);
            mServiceIsRunning = true;
        } else {
            Log.d(TAG, "DownloadService is already created and running.");
        }

        //TODO: Should I backup the previous collection?
        //TODO: Should the old files be removed or only add new files? (Add this as feature to be implemented in the future)
    }

    private List<SerializableMusicTrack> generateRandomList() {

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
                    return convertToSerializableMusicTracks(randomMusicTrackList);
                }
            }
        }
        return convertToSerializableMusicTracks(randomMusicTrackList);
    }

    private List<SerializableMusicTrack> convertToSerializableMusicTracks(List<MusicTrack> randomMusicTrackList) {
        List<SerializableMusicTrack> serializableRandomMusicTrackList = new ArrayList<SerializableMusicTrack>();
        for (MusicTrack track : randomMusicTrackList) {
            SerializableMusicTrack serializableMusicTrack = new SerializableMusicTrack();
            serializableMusicTrack.setAlbum(track.getAlbum());
            serializableMusicTrack.setFirstArtist(track.getFirstArtist() == null ? "" : track.getFirstArtist().toString());
            serializableMusicTrack.setMimetype(track.getFirstResource().getProtocolInfo().getContentFormatMimeType() == null ? "" : track.getFirstResource().getProtocolInfo().getContentFormatMimeType().toString());
            serializableMusicTrack.setOriginalTrackNumber(track.getOriginalTrackNumber() == null ? 0 : track.getOriginalTrackNumber());
            serializableMusicTrack.setTitle(track.getTitle());
            serializableMusicTrack.setURL(track.getFirstResource().getValue());
            serializableRandomMusicTrackList.add(serializableMusicTrack);
        }
        return serializableRandomMusicTrackList;
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

    private void displayResults() {
        Intent resultIntent = new Intent(this, ResultActivity.class);
        resultIntent.putExtra("Options", options);
        startActivity(resultIntent);
    }


    // Broadcast receiver for receiving status updates from the IntentService
    private class DownloadStateReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private DownloadStateReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressBar = (ProgressBar) findViewById((R.id.progressBar));
            mProgressBar.setProgress(intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, 0));
            Log.d(TAG, "onReceive called with value: " + intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, 0));
            if (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, 0) == 100) {
                displayResults();
            }
        }
    }

    private class BasicRegistryListener extends DefaultRegistryListener {
        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            findCorrectDevice(upnpService.getRegistry().getRemoteDevices());
        }
    }
}
