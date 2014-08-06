package fi.atteheino.mediashuffler.app;

import java.io.Serializable;

/**
 * Created by Atte on 7.6.2014.
 */
public class Options implements Serializable{

    private String DLNADevice;
    private String DLNADeviceUDN;

    public String getDLNADevice() {
        return DLNADevice;
    }

    public void setDLNADevice(String DLNADevice) {
        this.DLNADevice = DLNADevice;
    }

    public String getDLNADeviceUDN() {
        return DLNADeviceUDN;
    }

    public void setDLNADeviceUDN(String DLNADeviceUDN) {
        this.DLNADeviceUDN = DLNADeviceUDN;
    }
}
