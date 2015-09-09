package com.example.hyeonseob.beacontriangulation.Intro;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.R;

public class RotateActivity2 extends Activity implements SensorEventListener {
    private final static int ROTATE_ONCE = 150, NUMBER_OF_ROTATE = 15, ROTATE_THRESHOLD = ROTATE_ONCE * NUMBER_OF_ROTATE;

    private RelativeLayout mRelativeLayout;
    private SensorManager mSensManager;
    private Sensor mSensor;
    private float[] mSensorValue, mPrevValue;
    private float mProgress;
    private int mPercent;

    private ProgressBar mProgBar;
    private TextView mProgTextView, mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate2);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mProgBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgTextView = (TextView) findViewById(R.id.progressTextView);
        mTextView = (TextView) findViewById(R.id.textView);
        mProgress = 0f;
        mPrevValue = new float[3];

        mSensManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mProgress >= ROTATE_THRESHOLD) {
                    final Intent intent = new Intent(RotateActivity2.this, HoldActivity2.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mSensorValue = event.values.clone();
        mProgress += (Math.abs(mSensorValue[0]-mPrevValue[0])
                + Math.abs(mSensorValue[1]-mPrevValue[1])
                + Math.abs(mSensorValue[2]-mPrevValue[2]));
        mPrevValue = mSensorValue;

        mPercent = Math.min((int)(mProgress / ROTATE_THRESHOLD * 100), 100);
        mProgBar.setProgress(mPercent);
        mProgTextView.setText(mPercent+"%");
        if(mProgress >= ROTATE_THRESHOLD) {
            mTextView.setText("보정이 완료되었습니다!\n화면을 터치하세요.");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensManager.unregisterListener(this);
    }
}
