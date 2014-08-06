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
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;

import static junit.framework.Assert.assertEquals;


public class SourceFolderSelectActivity extends Activity {


    private Options options;

    AndroidUpnpService upnpService;

    ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
        }
        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_folder_select);

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );


        options = (Options)getIntent().getSerializableExtra("Options");
    }

    @Override
    protected void onResume() {
        super.onResume();



        new Browse((Service)upnpService, "0", BrowseFlag.DIRECT_CHILDREN) {

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
