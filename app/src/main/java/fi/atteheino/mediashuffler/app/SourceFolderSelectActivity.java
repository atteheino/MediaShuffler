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
import android.widget.TextView;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;

import java.util.Collection;
import java.util.List;


public class SourceFolderSelectActivity extends Activity {


    private Options options;

    AndroidUpnpService upnpService;
    private ServiceId serviceId = null;
    private Service mediaServerService;
    private String level = "0";
    private TextView sourceFolderTextView;

    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            serviceId = new UDAServiceId(options.getDLNADeviceUDN());
            // Add a listener for device registration events
            /*upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService)
            );*/

            // Broadcast a search message for all devices
            //upnpService.getControlPoint().search();

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

                                // Read the DIDL content either using generic Container and Item types...


                                StringBuilder sb = new StringBuilder();
                                List<Container> containers = didl.getContainers();
                                for (Container container : containers) {
                                    sb.append("ID: ").append(container.getId()).append(" ");
                                    sb.append("Title:").append(container.getTitle()).append(" ");
                                }
                                setSourceFolderTextViewText(sb.toString());
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

    /**
     * Tähän pitää kirjoittaa funktio joka tutkii jo rekisterissä olevien laitteiden palvelut ja
     * kiinnittyy oikeaan. Sitten voidaan lisätä kuuntelija browse metodille. Tätä rekisterikuuntelijaa ei tarvita
     * sillä sellainen on jo ja se on globaali.!!
     */


    private void setSourceFolderTextViewText(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sourceFolderTextView.setText(content);
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


        options = (Options)getIntent().getSerializableExtra("Options");
        if (getIntent().getStringExtra("selected_level") != null) {
            level = getIntent().getStringExtra("selected_level");
        }

        sourceFolderTextView = (TextView) findViewById(R.id.sourceFolderTextView);
        sourceFolderTextView.setOnClickListener(sourceFolderTextViewListener);
    }

    private View.OnClickListener sourceFolderTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent sourceFolderSelectActivity = new Intent(getApplicationContext(), SourceFolderSelectActivity.class);
            sourceFolderSelectActivity.putExtra("Options", options);
            sourceFolderSelectActivity.putExtra("selected_level", "1");
            startActivity(sourceFolderSelectActivity);
        }
    };

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
