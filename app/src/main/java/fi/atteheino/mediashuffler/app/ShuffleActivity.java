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

import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;


public class ShuffleActivity extends Activity {

    AndroidUpnpService upnpService;
    private Options options;
    private ShuffleFilesTask mShuffleTask;
    private ProgressBar mProgressBar;
    private Vector<String> URIs = new Vector<String>();

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
                    List<String> tempUris = (List<String>) o;
                    if (tempUris != null && tempUris.size() != 0) {
                        URIs.addAll(tempUris);
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
            return null;
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
