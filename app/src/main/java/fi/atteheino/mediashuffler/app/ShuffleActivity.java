package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.support.model.item.MusicTrack;

import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import fi.atteheino.mediashuffler.app.utils.Downloader;


public class ShuffleActivity extends Activity {

    AndroidUpnpService upnpService;
    private Options options;
    private ShuffleFilesTask mShuffleTask;
    private ProgressBar mProgressBar;
    private Vector<MusicTrack> musicTracks = new Vector<MusicTrack>();

    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
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
                // we shal add counter value.
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
                    transferFiles();
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

        final View cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCancelAdd();
            }
        });

    }

    public void transferFiles() {
        //Let's stop the progressbar and start feeding it correct values.
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(0);
        options.setMusicTrackList(generateRandomList());
        //TODO: Add MusicTracks to Options object
        //TODO: Start ShuffleFilesTask. Pass Options as parameter
        //TODO: Start processing URL's in MusicTracks collection.
        //TODO: Create random list of MusicTracks limited by the selected max size
        //TODO: Should I backup the previous collection?
        //TODO: Should the old files be removed or only add new files? (Add this as feature to be implemented in the future)
    }

    private List<MusicTrack> generateRandomList() {
        return null;
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


    private class ShuffleFilesTask extends AsyncTask<Options, Integer, String> {
        @Override
        protected String doInBackground(Options... optionses) {
            final List<MusicTrack> musicTrackList = optionses[0].getMusicTrackList();
            int counter = 0;
            for (MusicTrack track : musicTrackList) {
                Downloader.downloadFile(track.getFirstResource().getValue(),
                        optionses[0].getTargetFolderName(),
                        getFilename(track));
                counter++;
                publishProgress((counter / musicTrackList.size()) * 100);
            }

            return null;
        }

        private String getFilename(MusicTrack track) {
            return new StringBuilder().append(track.getOriginalTrackNumber())
                    .append(" ")
                    .append(track.getFirstArtist())
                    .append(" ")
                    .append(track.getTitle())
                    .append(getExtension(track)).toString();
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
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
        }
    }
}
