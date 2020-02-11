package id.ac.ui.clab.dchronochat.WiFi;

/**
 * Created by LittleBoy on 2018/4/27.
 */
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import id.ac.ui.clab.dchronochat.activity.WifiActivity;

public class ListenerThread extends Thread{

    private static final String LOG_TAG = "ListenerThread";
    private final String[] IPMatch = {"192","168","43"};
    private Handler handler;
    private WifiActivity activity;
    private boolean stop = false;

    public ListenerThread(Handler handler,WifiActivity activity){
        setName("ListenerThread");
        this.handler = handler;
        this.activity = activity;
    }


    @Override
    public void run() {
        while (!stop){
            //每过1s查找/proc/net/arp
            try {
                ArrayList<String> ipAddress = getConnectedIP();
                for(String ip : ipAddress){
                    if(match(ip)){
                        activity.setRemoteIP(ip);
                        handler.sendEmptyMessage(WifiActivity.DEVICE_CONNECTED);
                        stop = true;
                        break;
                    }
                }
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取连接到热点上的手机ip
     *
     * @return
     */
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<>();
        try {
            File arp = new File("/proc/net/arp");
            FileReader arpFileReader = new FileReader(arp);
            BufferedReader br = new BufferedReader(arpFileReader);
            String line;
            while ((line = br.readLine()) != null) {
                Log.i(LOG_TAG,"read /proc/net/arp :" + line);
                String[] splitted = line.split(" +");
                if (splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
            br.close();
            arpFileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }

    private boolean match(String ip){
        String[] ipInfo = ip.split("\\.");
        for (int i = 0;i < 3; ++i){
            if(!IPMatch[i].equals(ipInfo[i])){
                return false;
            }
        }
        return true;
    }

    public void Stop(){
        stop = true;
    }

}