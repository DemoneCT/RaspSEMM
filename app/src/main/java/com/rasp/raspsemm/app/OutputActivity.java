package com.rasp.raspsemm.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;


public class OutputActivity extends ActionBarActivity {

    //Intent intent = getIntent();


    //private Button bt;
    private TextView tv;
    private Socket socket;
    private String serverIpAddress = "10.42.0.1";
    private static final int REDIRECTED_SERVERPORT = 5002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        Bundle extras = getIntent().getExtras();
        String username = extras.getString("USERNAME");
        String password = extras.getString("PASSWORD");

        //Toast.makeText(getBaseContext(), username, Toast.LENGTH_LONG).show();
        //Toast.makeText(getBaseContext(), password, Toast.LENGTH_LONG).show();

        //bt = (Button) findViewById(R.id.myButton);
        tv = (TextView) findViewById(R.id.socket);
        try {
            InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
            socket = new Socket(serverAddr, REDIRECTED_SERVERPORT);
            OutputStream out = socket.getOutputStream();

            PrintWriter output = new PrintWriter(out);
            output.println("Hello Android!");
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String st = input.readLine();
            Toast.makeText(getBaseContext(), st, Toast.LENGTH_LONG).show();
            socket.close();

            //BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()))
            //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            //out.println(username);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

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
