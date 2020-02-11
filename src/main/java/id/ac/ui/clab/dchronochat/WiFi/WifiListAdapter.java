package id.ac.ui.clab.dchronochat.WiFi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import id.ac.ui.clab.dchronochat.R;

/**
 * Created by LittleBoy on 2018/4/27.
 */

public class WifiListAdapter extends ArrayAdapter<WifiScanResult> {


    private final LayoutInflater mInflater;
    private int mResource;

    public WifiListAdapter(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.wifi_name);
        ImageView signl = (ImageView) convertView.findViewById(R.id.wifi_signal);

        WifiScanResult scanResult = getItem(position);
        assert scanResult != null;
        name.setText(scanResult.SSID);

        int level = scanResult.level;
        if (level <= 0 && level >= -50) {
            signl.setImageResource(R.drawable.wifi4);
        } else if (level < -50 && level >= -70) {
            signl.setImageResource(R.drawable.wifi3);
        } else if (level < -70 && level >= -80) {
            signl.setImageResource(R.drawable.wifi2);
        } else if (level < -80 && level >= -100) {
            signl.setImageResource(R.drawable.wifi1);
        } else {
            signl.setImageResource(R.drawable.wifi0);
        }

        return convertView;
    }

}
