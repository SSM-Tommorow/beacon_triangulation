package com.example.hyeonseob.beacontriangulation.Activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.hyeonseob.beacontriangulation.R;

import java.util.Vector;


public class MapActivity extends Activity {
    private final static int GAP = 41;

    private int mapHeight, mapWidth;
    private int checkedButton = -1;

    private RelativeLayout mapLayout;
    private ImageView mapImageView;
    private Vector<ImageView> buttonView;

    private Drawable redButton;
    private Drawable grayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        redButton = getResources().getDrawable(R.drawable.red_button);
        grayButton = getResources().getDrawable(R.drawable.gray_button);
        mapLayout = (RelativeLayout) findViewById(R.id.mapFrameLayout);
        mapImageView = (ImageView) findViewById(R.id.mapImageView);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        mapHeight = mapImageView.getHeight();
        mapWidth = mapImageView.getWidth();
        Log.w("map", "" + mapHeight + "," + mapWidth);

        new BackgroundTask().execute();
    }

    class BackgroundTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            View.OnClickListener ocl = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.getId() != checkedButton)
                    {
                        if(checkedButton != -1) {
                            buttonView.elementAt(checkedButton).setImageDrawable(grayButton);
                            buttonView.elementAt(checkedButton).setImageAlpha(50);
                        }
                        checkedButton = v.getId();
                        ((ImageView)v).setImageDrawable(redButton);
                        ((ImageView)v).setImageAlpha(200);
                    }
                    else
                    {
                        checkedButton = -1;
                        ((ImageView)v).setImageDrawable(grayButton);
                        ((ImageView)v).setImageAlpha(50);
                    }
                }
            };

            RelativeLayout.LayoutParams param;
            int i,j,cnt=0;
            buttonView = new Vector<ImageView>();
            for(i=0; i<mapHeight; i+= GAP) {
                for(j=0; j<mapWidth; j+= GAP) {
                    ImageView imageView = new ImageView(MapActivity.this);
                    imageView.setImageDrawable(grayButton);
                    imageView.setId(cnt++);
                    imageView.setImageAlpha(50);
                    param = new RelativeLayout.LayoutParams(GAP, GAP);
                    param.topMargin = i;
                    param.leftMargin = j;
                    imageView.setLayoutParams(param);
                    imageView.setClickable(true);
                    imageView.setOnClickListener(ocl);
                    mapLayout.addView(imageView);
                    buttonView.add(imageView);
                }
            }
        }
    }
}
