package com.example.hyeonseob.beacontriangulation.Intro;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.R;

public class BluetoothActivity2 extends Activity {
    private RelativeLayout mRelativeLayout;
    private TextView mTextView, mTextView2;
    private boolean mBluetoothOn;

    public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";
    public static final boolean SCAN_RECO_ONLY = true;
    public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = true;
    public static final boolean DISCONTINUOUS_SCAN = false;

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth2);


        mBluetoothOn = false;
        mTextView = (TextView) findViewById(R.id.textView);
        mTextView2 = (TextView) findViewById(R.id.textView2);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mBluetoothOn) {
                    final Intent intent = new Intent(BluetoothActivity2.this, RotateActivity2.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        //If a user device turns off bluetooth, request to turn it on.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
        else if(mBluetoothAdapter.isEnabled()){
            mTextView.setText("블루투스가 실행되었습니다.\n측정하는 동안 연결을 유지해주세요.");
            mTextView2.setText("화면을 터치하세요.");
            mBluetoothOn = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            //If the request to turn on bluetooth is denied, the app will be finished.
            //finish();
            return;
        }
        else {
            mTextView.setText("블루투스가 실행되었습니다.\n측정하는 동안 연결을 유지해주세요.");
            mTextView2.setText("화면을 터치하세요.");
            mBluetoothOn = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
