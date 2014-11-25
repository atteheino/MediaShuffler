package fi.atteheino.mediashuffler.app;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Atte on 28.10.2014.
 */
public class DLNADirectoryBrowser extends Observable {

    private RemoteService remoteService;
    private AndroidUpnpService upnpService;
    private Observer observer;

    public DLNADirectoryBrowser(RemoteService remoteService, AndroidUpnpService upnpService, Observer observer) {
        this.remoteService = remoteService;
        this.upnpService = upnpService;
        this.observer = observer;
        addObserver(observer);
    }

    /**
     * @param sourceFolderID the folder to descend into
     */
    public void browseThruFoldersAndGetFileUris(final String sourceFolderID) {
        ActionCallback callback = new Browse((Service) remoteService, sourceFolderID, BrowseFlag.DIRECT_CHILDREN) {
            @Override
            public void received(ActionInvocation actionInvocation, DIDLContent didl) {
                List<MusicTrack> tracks = new ArrayList<MusicTrack>();
                //Get list of files in the folder and add them to the global list of files..
                final List<Item> items = didl.getItems();
                for (Item item : items) {
                    //Let's check that this is actually a music file before adding.
                    if (MusicTrack.CLASS.equals(item)) {
                        tracks.add((MusicTrack) item);
                    }
                }
                //Get sub-folders and start to descend into them
                final List<Container> containers = didl.getContainers();
                for (Container container : containers) {
                    final DLNADirectoryBrowser dlnaDirectoryBrowser = new DLNADirectoryBrowser(remoteService, upnpService, observer);
                    dlnaDirectoryBrowser.browseThruFoldersAndGetFileUris(container.getId());
                }
                setChanged();
                notifyObservers(tracks);

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
        setChanged();
        notifyObservers();

    }

}
