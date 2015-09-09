package com.example.hyeonseob.beacontriangulation.Intro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.hyeonseob.beacontriangulation.R;


public class HoldActivity2 extends Activity {
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold2);

        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final Intent intent = new Intent(HoldActivity2.this, StartingActivity2.class);
                startActivity(intent);
                finish();

                return false;
            }
        });
    }
}
