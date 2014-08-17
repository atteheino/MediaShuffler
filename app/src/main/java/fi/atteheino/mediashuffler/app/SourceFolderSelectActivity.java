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

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.item.Item;


public class SourceFolderSelectActivity extends Activity {


    private Options options;

    AndroidUpnpService upnpService;
    private ServiceId serviceId = null;
    private Service mediaServerService;

    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            serviceId = new UDAServiceId(options.getDLNADeviceUDN());
            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService)
            );

            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(

            );
        }
        public void onServiceDisconnected(ComponentName className) {
            upnpService.getRegistry().removeAllLocalDevices();
            upnpService.getRegistry().removeAllRemoteDevices();
            upnpService = null;

        }
    };

    /**
     * Tähän pitää kirjoittaa funktio joka tutkii jo rekisterissä olevien laitteiden palvelut ja
     * kiinnittyy oikeaan. Sitten voidaan lisätä kuuntelija browse metodille. Tätä rekisterikuuntelijaa ei tarvita
     * sillä sellainen on jo ja se on globaali.!!
     */



    private RegistryListener createRegistryListener(final AndroidUpnpService upnpService) {
    return new DefaultRegistryListener() {

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

            System.out.println("Device Was found: " + device.getDisplayString());
            if (device.getIdentity().getUdn().getIdentifierString().equals(options.getDLNADeviceUDN())) {

                RemoteService[] services = device.getServices();
                for (Service service : services) {
                    if (service.getServiceType().equals("urn:upnp-org:serviceId:ContentDirectory")) {
                        mediaServerService = service;
                    }
                }
                /*if ((mediaServerService = ) != null) {

                    System.out.println("Service discovered: " + mediaServerService);
                    //executeAction(upnpService, switchPower);

                } else {
                    System.out.println("Ei osunut");
                }*/

                ActionCallback complexBrowseAction =
                        new Browse(mediaServerService, "0", BrowseFlag.DIRECT_CHILDREN) {

                            @Override
                            public void received(ActionInvocation actionInvocation, DIDLContent didl) {

                                // Read the DIDL content either using generic Container and Item types...

                                Item item1 = didl.getItems().get(0);

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
            } else {
                System.out.println("Device was not a match");
            }
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {

            if ((mediaServerService = device.findService(serviceId)) != null) {
                System.out.println("Service disappeared: " + mediaServerService);
            }
        }

    };
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
    }

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
