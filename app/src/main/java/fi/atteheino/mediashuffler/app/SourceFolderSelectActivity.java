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
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;


public class SourceFolderSelectActivity extends Activity {


    private Options options;

    AndroidUpnpService upnpService;

    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService)
            );

            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(
                    new STAllHeader()
            );
        }
        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    private RegistryListener createRegistryListener(final AndroidUpnpService upnpService) {
    return new DefaultRegistryListener() {

        ServiceId serviceId = new UDAServiceId(options.getDLNADeviceUDN());

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

            Service switchPower;
            if ((switchPower = device.findService(serviceId)) != null) {

                System.out.println("Service discovered: " + switchPower);
                //executeAction(upnpService, switchPower);

            } else {
                System.out.println("Ei osunut");
            }

        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Service switchPower;
            if ((switchPower = device.findService(serviceId)) != null) {
                System.out.println("Service disappeared: " + switchPower);
            }
        }

    };
}
    /*
    private Browse myBrowse(AndroidUpnpService upnpService) {
        return new Browse((Service)upnpService, "0", BrowseFlag.DIRECT_CHILDREN) {

        @Override
        public void received(ActionInvocation actionInvocation, DIDLContent didl) {

            // Read the DIDL content either using generic Container and Item types...
            assertEquals(didl.getItems().size(), 2);
            Item item1 = didl.getItems().get(0);
            assertEquals(
                    item1.getTitle(),
                    "All Secrets Known"
            );
            assertEquals(
                    item1.getFirstPropertyValue(DIDLObject.Property.UPNP.ALBUM.class),
                    "Black Gives Way To Blue"
            );
            assertEquals(
                    item1.getFirstResource().getProtocolInfo().getContentFormatMimeType().toString(),
                    "audio/mpeg"
            );
            assertEquals(
                    item1.getFirstResource().getValue(),
                    "http://10.0.0.1/files/101.mp3"
            );

            // ... or cast it if you are sure about its type ...
            assert MusicTrack.CLASS.equals(item1);
            MusicTrack track1 = (MusicTrack) item1;
            assertEquals(track1.getTitle(), "All Secrets Known");
            assertEquals(track1.getAlbum(), "Black Gives Way To Blue");
            assertEquals(track1.getFirstArtist().getName(), "Alice In Chains");
            assertEquals(track1.getFirstArtist().getRole(), "Performer");

            MusicTrack track2 = (MusicTrack) didl.getItems().get(1);
            assertEquals(track2.getTitle(), "Check My Brain");

            // ... which is much nicer for manual parsing, of course!

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
*/

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
