package com.example.hyeonseob.beacontriangulation.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.hyeonseob.beacontriangulation.R;


public class ConfigurationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
    }

    public void onButtonClicked(View v) {
        Button btn = (Button)v;
        if(btn.getId() == R.id.showMapButton) {
            final Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        }
        else if(btn.getId() == R.id.mapUpdateButton) {
            final Intent intent = new Intent(this, MapUpdateActivity.class);
            startActivity(intent);
        }
    }
}
