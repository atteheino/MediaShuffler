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
package fi.atteheino.mediashuffler.app.activity;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.SwitchableRouter;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.atteheino.mediashuffler.app.BrowserUpnpService;
import fi.atteheino.mediashuffler.app.Options;
import fi.atteheino.mediashuffler.app.R;


public class BrowserActivity extends ListActivity {

    static final Comparator<DeviceDisplay> DISPLAY_COMPARATOR =
            new Comparator<DeviceDisplay>() {
                public int compare(DeviceDisplay a, DeviceDisplay b) {
                    return a.toString().compareTo(b.toString());
                }
            };
    private ArrayAdapter<DeviceDisplay> listAdapter;
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private AndroidUpnpService upnpService;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Refresh the list with all known devices
            listAdapter.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };
    private Options options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        setListAdapter(listAdapter);

        getApplicationContext().bindService(
                new Intent(this, BrowserUpnpService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        options = (Options) getIntent().getSerializableExtra("Options");

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        DeviceDisplay selectedDevice = (DeviceDisplay) l.getItemAtPosition(position);
        options.setDLNADevice(selectedDevice.toString());
        options.setDLNADeviceUDN(selectedDevice.device.getIdentity().getUdn().getIdentifierString());

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("Options", options);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        menu.add(0, 0, 0, R.string.search_lan).setIcon(android.R.drawable.ic_menu_search);
        menu.add(0, 1, 0, R.string.switch_router).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 2, 0, R.string.toggle_debug_logging).setIcon(android.R.drawable.ic_menu_info_details);

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
        switch (item.getItemId()) {
            case 0:
                searchNetwork();
                break;
            case 1:
                if (upnpService != null) {
                    SwitchableRouter router = (SwitchableRouter) upnpService.get().getRouter();
                    if (router.isEnabled()) {
                        Toast.makeText(this, R.string.disabling_router, Toast.LENGTH_SHORT).show();
                        router.disable();
                    } else {
                        Toast.makeText(this, R.string.enabling_router, Toast.LENGTH_SHORT).show();
                        router.enable();
                    }
                }
                break;
            case 2:
                Logger logger = Logger.getLogger("org.teleal.cling");
                if (logger.getLevel().equals(Level.FINEST)) {
                    Toast.makeText(this, R.string.disabling_debug_logging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.INFO);
                } else {
                    Toast.makeText(this, R.string.enabling_debug_logging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.FINEST);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void searchNetwork() {
        if (upnpService == null) return;
        Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
        upnpService.getRegistry().removeAllRemoteDevices();
        upnpService.getControlPoint().search();
    }

    protected void showToast(final String msg, final boolean longLength) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(
                        BrowserActivity.this,
                        msg,
                        longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            showToast(
                    "Discovery failed of '" + device.getDisplayString() + "': " +
                            (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                    true
            );
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);

                    int position = listAdapter.getPosition(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same position
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }

                    // Sort it?
                    // listAdapter.sort(DISPLAY_COMPARATOR);
                    // listAdapter.notifyDataSetChanged();
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    protected class DeviceDisplay {

        Device device;

        public DeviceDisplay(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public String toString() {
            String name =
                    device.getDetails() != null && device.getDetails().getFriendlyName() != null && device.getType().getType() != null
                            ? device.getDetails().getFriendlyName() + " " + device.getType().getType()
                            : device.getDisplayString() + " " + device.getType().getType();
            // Display a little star while the device is being loaded (see performance optimization earlier)
            return device.isFullyHydrated() ? name : name + " *";
        }
    }
}
