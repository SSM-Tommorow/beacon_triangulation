package com.example.hyeonseob.beacontriangulation.Intro;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Activity.ConfigurationActivity;
import com.example.hyeonseob.beacontriangulation.Class.DeviceManager;
import com.example.hyeonseob.beacontriangulation.R;


public class MainActivity extends Activity {
    public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";
    public static final boolean SCAN_RECO_ONLY = true;
    public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = true;
    public static final boolean DISCONTINUOUS_SCAN = false;

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If a user device turns off bluetooth, request to turn it on.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }

        DeviceManager mDeviceManager = new DeviceManager(getApplicationContext());
        ((TextView)findViewById(R.id.androidIdTextView)).setText(mDeviceManager.getDeviceString());
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
                //If the request to turn on bluetooth is denied, the app will be finished.
                finish();
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        Log.i("MainActivity", "onResume()");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i("MainActivity", "onDestroy");
        super.onDestroy();
    }

    public void onButtonClicked(View v) {
        Log.i("button","button clicked!");
        Button btn = (Button)v;
        if(btn.getId() == R.id.startNavigatingButton) {
            final Intent intent = new Intent(this, BluetoothActivity.class);
            startActivity(intent);
        } else if(btn.getId() == R.id.manageFingerprintButton){
            final Intent intent = new Intent(this, ConfigurationActivity.class);
            startActivity(intent);
        }
    }
}
