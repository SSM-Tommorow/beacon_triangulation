package com.example.hyeonseob.beacontriangulation.Intro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Activity.BeaconTestActivity;
import com.example.hyeonseob.beacontriangulation.Activity.ConfigurationActivity;
import com.example.hyeonseob.beacontriangulation.Class.DeviceManager;
import com.example.hyeonseob.beacontriangulation.R;


public class MainActivity extends Activity {
    public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";
    public static final boolean SCAN_RECO_ONLY = true;
    public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = true;
    public static final boolean DISCONTINUOUS_SCAN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DeviceManager mDeviceManager = new DeviceManager(getApplicationContext());
        ((TextView)findViewById(R.id.androidIdTextView)).setText("Device: "+mDeviceManager.getDeviceString());
    }

    public void onButtonClicked(View v) {
        Button btn = (Button)v;
        if(btn.getId() == R.id.startNavigatingButton) {
            final Intent intent = new Intent(this, BluetoothActivity.class);
            startActivity(intent);
        } else if(btn.getId() == R.id.manageFingerprintButton){
            final Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
        } else if(btn.getId() == R.id.multipleUserButton){
            final Intent intent = new Intent(this, BluetoothActivity2.class);
            startActivity(intent);
        } else if(btn.getId() == R.id.beaconTestButton){
            final Intent intent = new Intent(this, BeaconTestActivity.class);
            startActivity(intent);
        }
    }
}
