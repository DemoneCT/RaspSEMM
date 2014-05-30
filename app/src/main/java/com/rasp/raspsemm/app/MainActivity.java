package com.rasp.raspsemm.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import android.util.Log;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        {

    WifiManager mainWifiObj;
    WifiScanReceiver wifiReciever;
    ListView list;
    String wifis[];


    final String serverIpAddress = "10.42.0.1";
    private static final String TAG_SUCCESS = "success";


    Intent loginIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        list = (ListView)findViewById(R.id.listView1);

        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        loginIntent = new Intent(this, OutputActivity.class);

        // Check for WiFi is disabled
        if (mainWifiObj.isWifiEnabled() == false) {
            // If WiFi disabled then enable it
            Toast.makeText(getApplicationContext(), "Wi-fi is disabled...making it enabled", Toast.LENGTH_LONG).show();

            mainWifiObj.setWifiEnabled(true);
        }

        wifiReciever = new WifiScanReceiver();
        mainWifiObj.startScan();
        list.setClickable(true);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Object o = list.getItemAtPosition(position);
                String str=(String)o;
                if (str.equals("semm")) {

                    //If Network is not already added

                    if (!WifiNetworkExists()) {

                        WifiConfiguration wc = new WifiConfiguration();
                        wc.SSID = "\"semm\"";
                        wc.preSharedKey = "\"raspberry\"";
                        wc.hiddenSSID = true;
                        wc.status = WifiConfiguration.Status.ENABLED;

                        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

                        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {

                            try {

                                setIpAssignment("STATIC", wc); // for setting static IP
                                setIpAddress(InetAddress.getByName("10.42.0.2"), 24, wc);
                                setGateway(InetAddress.getByName("10.42.0.1"), wc);
                                setDNS(InetAddress.getByName("8.8.8.8"), wc);

                                //mainWifiObj.updateNetwork(wc); //apply the setting

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

                            // Only for Honeycomb and older versions
                            android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_USE_STATIC_IP, "1");
                            android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_IP, "10.42.0.2");
                            android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_NETMASK, "255.255.255.0");
                            android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS1, "8.8.8.8");
                            android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_GATEWAY, "10.42.0.1");

                        }

                        int res = mainWifiObj.addNetwork(wc);
                        Log.v("WifiPreference", "add Network returned " + res);
                        boolean b = mainWifiObj.enableNetwork(res, true);
                        Log.v("WifiPreference", "enableNetwork returned " + b);

                    }

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
                        Dialog dialog = new Dialog(MainActivity.this);
                        //dialog.setTitle("LOGIN");
                        builder.setView(inflater.inflate(R.layout.dialog_signin, null));
                        builder.setTitle("LOGIN");


                        builder.setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button

                                //For issue android.os.NetworkOnMainThreadException
                                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                        .permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                }

                                Dialog f = (Dialog) dialog;

                                final EditText us_et = (EditText) f.findViewById(R.id.username);
                                final EditText pw_et = (EditText) f.findViewById(R.id.password);
                                String us = us_et.getText().toString();
                                String pw = pw_et.getText().toString();

                                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                                pairs.add(new BasicNameValuePair("username", us));
                                pairs.add(new BasicNameValuePair("password", pw));

                                //***JSON***
                                JSONParser jsonParser = new JSONParser();
                                JSONObject json = jsonParser.makeHttpRequest("http://"+serverIpAddress+"/login.php",
                                        "GET", pairs);

                                // Check log cat console from response
                                Log.v("Login Response", json.toString());

                                try {
                                    if(json.getInt(TAG_SUCCESS)==1) {

                                        String id_users = json.getString("id");
                                        loginIntent.putExtra("username", us);
                                        loginIntent.putExtra("password", pw);
                                        loginIntent.putExtra("id_users", id_users);
                                        startActivity(loginIntent);

                                    }
                                    else {
                                        Toast.makeText(getBaseContext(),"User or Password incorrect!",Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        dialog = builder.create();
                        dialog.show();
                }
                else {
                    Toast.makeText(getBaseContext(),"Wifi network not correct!",Toast.LENGTH_SHORT).show();

                }
            }

        });
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            wifis = new String[wifiScanList.size()];
            for(int i = 0; i < wifiScanList.size(); i++){

                //Populating the WiFi network list
                wifis[i] = (wifiScanList.get(i).SSID);

            }

            list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1,wifis));
        }
    }


    boolean WifiNetworkExists () {
        for (WifiConfiguration config : mainWifiObj.getConfiguredNetworks()) {
            String newSSID = config.SSID;

            if (config.SSID.equals("\"semm\"")) {

                Log.v("Network " + config.SSID + " already present", "Reconnect...");

                mainWifiObj.disconnect();
                mainWifiObj.enableNetwork(config.networkId, true);
                mainWifiObj.reconnect();

                return true;
            }
        }

        return false;
    }


    public static void setIpAssignment(String assign , WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
        setEnumField(wifiConf, assign, "ipAssignment");
    }

    public static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException{
        Object linkProperties = getField(wifiConf, "linkProperties");
        if(linkProperties == null)return;
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(new Class[]{InetAddress.class, int.class});
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList)getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException{
        Object linkProperties = getField(wifiConf, "linkProperties");
        if(linkProperties == null)return;
        Class routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor routeInfoConstructor = routeInfoClass.getConstructor(new Class[]{InetAddress.class});
        Object routeInfo = routeInfoConstructor.newInstance(gateway);

        ArrayList mRoutes = (ArrayList)getDeclaredField(linkProperties, "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    public static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
        Object linkProperties = getField(wifiConf, "linkProperties");
        if(linkProperties == null)return;

        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>)getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); //or add a new dns address , here I just want to replace DNS1
        mDnses.add(dns);
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.output, menu);
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

