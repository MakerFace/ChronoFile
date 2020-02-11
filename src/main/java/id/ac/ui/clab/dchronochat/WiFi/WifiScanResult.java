package id.ac.ui.clab.dchronochat.WiFi;

import android.net.wifi.ScanResult;

public class WifiScanResult {
    public String SSID;
    public int level;
    public String capabilities;

    public WifiScanResult(ScanResult result){
        this.SSID = result.SSID;
        this.level = result.level;
        this.capabilities = result.capabilities;
    }

}
