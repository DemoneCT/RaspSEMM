package com.rasp.raspsemm.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LogActivity extends ActionBarActivity {

    ArrayList<HashMap<String, String>> measuresList;
    JSONArray measures = null;

    ListView loglist;
    final String serverIpAddress = "10.42.0.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        loglist = (ListView)findViewById(R.id.listView);
        loglist.setClickable(false);

        Bundle extras = getIntent().getExtras();
        final String id_users = extras.getString("id_users");

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("id_users", id_users));


        //***JSON***
        JSONParser jsonParser = new JSONParser();
        JSONObject json = jsonParser.makeHttpRequest("http://"+serverIpAddress+"/read.php",
                "GET", pairs);

        // Check log cat console from response
        Log.v("LOG Response", json.toString());

        measuresList = new ArrayList<HashMap<String, String>>();

        try {
            measures = json.getJSONArray("measures");
            //Populating the HashMap with measured values and their timestamps.
            for(int i=0; i<measures.length(); i++) {

                JSONObject c = measures.getJSONObject(i);
                String value = c.getString("value");
                String date = c.getString("date");
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("value", value);
                map.put("date", date);
                measuresList.add(map);


                ListAdapter adapter = new SimpleAdapter(getApplicationContext(), measuresList, R.layout.list_item,
                        new String[]{"value","date"}, new int[]{R.id.value, R.id.date});
                loglist.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log, menu);
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
    */
}
