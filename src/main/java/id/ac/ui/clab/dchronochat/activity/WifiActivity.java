package id.ac.ui.clab.dchronochat.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import id.ac.ui.clab.dchronochat.R;
import id.ac.ui.clab.dchronochat.WiFi.ListenerThread;
import id.ac.ui.clab.dchronochat.WiFi.WifiListAdapter;
import id.ac.ui.clab.dchronochat.WiFi.WifiScanResult;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by LittleBoy on 2018/4/27.
 */

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "Wifi";
    private boolean isOpen = false;
    private boolean isListen = false;
    private TextView textview;
    private TextView text_state;

    private WifiManager wifiManager;
    private WifiListAdapter wifiListAdapter;
    private WifiConfiguration config;
    private int configurationID;
    private String remoteIP = "";

    /**
     * 热点名称
     */
    private static final String WIFI_HOTSPOT_SSID = "NDNTransmission";
    private static final String WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    private static final int WIFICIPHER_NOPASS = 1;
    private static final int WIFICIPHER_WEP = 2;
    private static final int WIFICIPHER_WPA = 3;

    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 2;//有设备连上热点

    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2;
    /**
     * 监听线程
     */
    private ListenerThread listenerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        initVIew();
        initBroadcastReceiver();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        checkPermission();
    }

    private void checkPermission() {

        List<String> permissionsList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[0]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                        (permissions.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    search();
                    //list is still empty
                } else {
                    // Permission Denied
                    Toast.makeText(this, "permission deny", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    private void initVIew() {
        ListView listView = (ListView) findViewById(R.id.listView);
        Button btn_create_hostspot = (Button) findViewById(R.id.btn_create_hostspot);
        Button btn_search = (Button) findViewById(R.id.btn_search);
        textview = (TextView) findViewById(R.id.textview);
        text_state = (TextView) findViewById(R.id.text_state);
        GifImageView mGif = (GifImageView) findViewById((R.id.img_gif));

        assert mGif != null;
        mGif.setImageResource(R.drawable.wifi);
        assert btn_create_hostspot != null;
        btn_create_hostspot.setOnClickListener(this);
        assert btn_search != null;
        btn_search.setOnClickListener(this);

        wifiListAdapter = new WifiListAdapter(this, R.layout.wifi_list_item);
        assert listView != null;
        listView.setAdapter(wifiListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                wifiManager.disconnect();
                final WifiScanResult scanResult = wifiListAdapter.getItem(position);
                assert scanResult != null;
                String capabilities = scanResult.capabilities;
                int type = WIFICIPHER_WPA;
                if (!TextUtils.isEmpty(capabilities)) {
                    if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                        type = WIFICIPHER_WEP;
                    } else {
                        type = WIFICIPHER_NOPASS;
                    }
                }
                isWiFiConfigurationExsits(scanResult.SSID);
                Log.i("WifiActivity", "onItemClick: " + config);
                if (config == null) {
                    if (type != WIFICIPHER_NOPASS) {
                        //需要密码
                        Log.i("WifiActivity", "onItemClick: need password");
                        final EditText editText = new EditText(WifiActivity.this);
                        final int finalType = type;
                        new AlertDialog.Builder(WifiActivity.this).setTitle("请输入Wifi密码").setIcon(
                                android.R.drawable.ic_dialog_info).setView(
                                editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(LOG_TAG, "editText.getText():" + editText.getText());
                                config = createWifiInfo(scanResult.SSID, editText.getText().toString(), finalType);
                                connect(config);
                            }
                        }).setNegativeButton("取消", null).show();
                    } else {
                        Log.i("WifiActivity", "onItemClick: don't need password");
                        config = createWifiInfo(scanResult.SSID, "", type);
                        connect(config);
                    }
                } else {
                    Log.i("WifiActivity", "onItemClick: has been confided");
                    connect(config);
                }
            }
        });
    }

    private void connect(WifiConfiguration config) {
        text_state.setText("连接中...");
        //去掉ssid中的""
        config.SSID = config.SSID.substring(1, config.SSID.length() - 1);
        Log.i("WifiActivity", "connect: SSID = " + config.SSID);
        int wcgID = wifiManager.addNetwork(config);
        Log.i("WifiActivity", "connect: wcgid = " + wcgID);
        boolean success = wifiManager.enableNetwork(wcgID, true);
        if (success) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            String strIP = intToInternetAddress(dhcpInfo.serverAddress);
            Log.i("WifiActivity", "connect: " + strIP);
        } else {
            text_state.setText("连接失败");
        }
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WIFI_AP_STATE_CHANGED);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    private void stopListener() {
        if (!isListen)
            return;
        if (listenerThread != null) {
            listenerThread.Stop();
            listenerThread = null;
            isListen = false;
        }
    }

    private void startListener() {
        if (isListen)
            return;
        if (listenerThread == null) {
            listenerThread = new ListenerThread(handler, this);
            listenerThread.start();
            isListen = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_hostspot:
                if (!isOpen) {
                    createWifiHotspot();
                } else {
                    closeWifiHotspot();
                    isOpen = false;
                }
                break;
            case R.id.btn_search:
                search();
                break;
        }
    }

    /**
     * 创建Wifi热点
     */
    @SuppressLint("SetTextI18n")
    private void createWifiHotspot() {
        if (wifiManager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi,
            wifiManager.setWifiEnabled(false);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = WIFI_HOTSPOT_SSID;
        config.preSharedKey = "123456789";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        //通过反射调用设置热点
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(this)) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                }
            }
            boolean enable = (Boolean) method.invoke(wifiManager, config, true);
            if (enable) {
                textview.setText("热点已开启 SSID:".concat(WIFI_HOTSPOT_SSID).concat(" password:123456789"));
                isOpen = true;
                startListener();
            } else {
                textview.setText("创建热点失败");
                stopListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
            textview.setText("创建热点失败");
        }
    }

    /**
     * 关闭WiFi热点
     */
    @SuppressLint("SetTextI18n")
    public void closeWifiHotspot() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        textview.setText("热点已关闭");
        text_state.setText("wifi已关闭");
        stopListener();
    }

    /**
     * 搜索wifi热点
     */
    private void search() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "未打开GPS，无法扫描", Toast.LENGTH_SHORT).show();
        }
        if (!wifiManager.isWifiEnabled()) {
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }
        wifiManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void startMainActivity() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.sharedPrefs), MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        Log.i(LOG_TAG, "put string :" + remoteIP);
        edit.putString(getString(R.string.remoteIP), remoteIP);
        edit.apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTING:
                    Log.i(LOG_TAG, "Device Connecting");
                    break;
                case DEVICE_CONNECTED:
                    textview.setText("设备连接成功，IP=" + remoteIP);
                    stopListener();
                    startMainActivity();
                    finish();
                    break;
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            assert action != null;
            switch (action) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION: {
                    Log.i(LOG_TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
                    // wifi已成功扫描到可用wifi。
                    List<ScanResult> _scanResults = wifiManager.getScanResults();
                    List<WifiScanResult> scanResults = new ArrayList<>();
                    for (int i = 0; i < _scanResults.size(); i++) {
                        if (WIFI_HOTSPOT_SSID.equals(_scanResults.get(i).SSID))
                            scanResults.add(new WifiScanResult(_scanResults.get(i)));
                    }
                    Log.i("WifiActivity", "onReceive: " + scanResults.size());
                    wifiListAdapter.clear();
                    wifiListAdapter.addAll(scanResults);
                    break;
                }
                case WifiManager.WIFI_STATE_CHANGED_ACTION: {
                    Log.i(LOG_TAG, "WifiManager.WIFI_STATE_CHANGED_ACTION");
                    int wifiState = intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_ENABLED:
                            //获取到wifi开启的广播时，开始扫描
                            Log.i("WifiActivity", "onReceive: start scan");
                            wifiManager.startScan();
                            break;
                        case WifiManager.WIFI_STATE_DISABLED:
                            //wifi关闭发出的广播
                            break;
                    }
                    break;
                }
                case WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                    //Log.i(LOG_TAG, "WifiManager.NETWORK_STATE_CHANGED_ACTION");
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        Log.i(LOG_TAG, "连接已断开");
                        text_state.setText("连接已断开");
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                        Log.i(LOG_TAG, "Connected");
                    } else {
                        NetworkInfo.DetailedState state = info.getDetailedState();
                        if (state == NetworkInfo.DetailedState.CONNECTING) {
                            text_state.setText("连接中...");
                        } else if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                            text_state.setText("正在验证身份信息...");
                        } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                            text_state.setText("正在获取IP地址...");
                        } else if (state == NetworkInfo.DetailedState.FAILED) {
                            text_state.setText("连接失败");
                        }
                    }
                    break;
                }
                case ConnectivityManager.CONNECTIVITY_ACTION: {
                    Log.i(LOG_TAG, "Connectivity Manager");
                    ConnectivityManager connManager =
                            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    assert connManager != null;
                    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifi.isConnected()) {
                        Log.i(LOG_TAG, "连接");

                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        assert wifiManager != null;
                        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        text_state.setText("已连接到网络:" + wifiInfo.getSSID());
                        //TODO:华为手机获取到Wifi信息时，自己加了双引号
                        String wifiHotSpot = "\"" + WIFI_HOTSPOT_SSID + "\"";
                        Log.i(LOG_TAG, "wifiInfo.getSSID():" + wifiInfo.getSSID() + "  WIFI_HOTSPOT_SSID:" + wifiHotSpot);
                        if (wifiInfo.getSSID().equals(wifiHotSpot)) {
                            //如果当前连接到的wifi是热点,则开启连接线程
                            DhcpInfo info = wifiManager.getDhcpInfo();
                            remoteIP = intToInternetAddress(info.serverAddress);
                            Log.i(LOG_TAG, remoteIP);
                            startMainActivity();
                            finish();
                        }
                    }
                }
                break;
                case WIFI_AP_STATE_CHANGED:
                    int state = intent.getIntExtra("wifi_state", 0);
                    Log.i("WifiActivity", "onReceive: state = " + state);
                    switch (state) {
                        case 13:
                            Log.i("WifiActivity", "onReceive: opened");
                            startListener();
                            isOpen = true;
                            break;
                        case 11:
                        case 10:
                        case 12:
                            stopListener();
                            isOpen = false;
                            break;
                    }
            }
        }
    };

    public void setRemoteIP(String ip) {
        this.remoteIP = ip;
    }

    /**
     * 判断当前wifi是否有保存
     *
     * @param SSID Wifi_SSID
     * @return WifiConfig
     */
    private void isWiFiConfigurationExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        configurationID = -1;
        for (WifiConfiguration existingConfig : existingConfigs) {
            configurationID++;
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                config = existingConfig;
                return;
            }
        }
        config = null;
    }

    public WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        Log.i(LOG_TAG, "SSID = " + SSID + "password " + password + "type =" + type);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    private static String intToInternetAddress(int hostAddress) {
        return (hostAddress & 0xFF) + "." + ((hostAddress >> 8) & 0xFF) + "." + ((hostAddress >> 16) & 0xFF) + "."
                + ((hostAddress >> 24) & 0xFF);
    }
}
