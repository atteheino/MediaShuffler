package fi.atteheino.mediashuffler.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.atteheino.mediashuffler.app.adapter.FolderSelectArrayAdapter;


public class SourceFolderSelectActivity extends Activity {


    AndroidUpnpService upnpService;
    FolderSelectArrayAdapter adapter;
    private String level = "0";
    private Options options;


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
                        ActionCallback callback = new Browse((Service) remoteService, level, BrowseFlag.DIRECT_CHILDREN) {
                            @Override
                            public void received(ActionInvocation actionInvocation, DIDLContent didl) {

                                List<Folder> folders = new ArrayList<Folder>();
                                List<Container> containers = didl.getContainers();
                                for (Container container : containers) {
                                    folders.add(new Folder(container.getId(), container.getTitle()));
                                }
                                updateAdapter(folders);
                            }

                            @Override
                            public void updateStatus(Browse.Status status) {
                                // Called before and after loading the DIDL content
                            }

                            @Override
                            public void failure(ActionInvocation invocation,
                                                UpnpResponse operation,
                                                String defaultMsg) {
                                // Something wasn't right...
                            }
                        };

                        upnpService.getControlPoint().execute(callback);
                    }
                }
            }
        }
    }


    private void updateAdapter(final List<Folder> folders){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                adapter.addAll(folders);
                adapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_folder_select);

        getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        // Get the options needed
        options = (Options)getIntent().getSerializableExtra("Options");
        if (getIntent().getStringExtra("selected_level") != null) {
            level = getIntent().getStringExtra("selected_level");
        }

        adapter = new FolderSelectArrayAdapter(this, R.layout.folder_select_array_adapter);
        ListView listView = (ListView) findViewById(R.id.sourceFolderListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mMessageClickedHandler);

    }

    // Create a message handling object as an anonymous class.
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            Intent sourceFolderSelectActivity = new Intent(getApplicationContext(), SourceFolderSelectActivity.class);
            sourceFolderSelectActivity.putExtra("Options", options);
            TextView idView = (TextView) v.findViewById(R.id.folderIdTextView);
            sourceFolderSelectActivity.putExtra("selected_level", idView.getText().toString());
            startActivity(sourceFolderSelectActivity);
        }
    };


   /* private View.OnClickListener sourceFolderTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent sourceFolderSelectActivity = new Intent(getApplicationContext(), SourceFolderSelectActivity.class);
            sourceFolderSelectActivity.putExtra("Options", options);
            sourceFolderSelectActivity.putExtra("selected_level", "1");
            startActivity(sourceFolderSelectActivity);
        }
    };*/

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.source_folder_select, menu);
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

}
