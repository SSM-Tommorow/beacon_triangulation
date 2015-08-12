package com.example.hyeonseob.beacontriangulation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Vector;

public class MapUpdateActivity extends Activity {
    private int mapHeight, mapWidth, mBlockSize;
    private int checkedButton = -1;

    private RelativeLayout mapLayout;
    private ImageView mapImageView;
    private Vector<ImageView> buttonView;
    private TextView mXTextView, mYTextView, mIDTextView, mIntervalTextView, mRSSITextView, mStatusTextView;

    private Drawable redButton;
    private Drawable grayButton;

    private TransCoordinate mTransCoord;
    private DBManager mDBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_update);

        redButton = getResources().getDrawable(R.drawable.red_button);
        grayButton = getResources().getDrawable(R.drawable.gray_button);
        mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        mapImageView = (ImageView) findViewById(R.id.mapImageView);
        mXTextView = (TextView) findViewById(R.id.xTextView);
        mYTextView = (TextView) findViewById(R.id.yTextView);
        mIDTextView = (TextView) findViewById(R.id.idTextView);
        mIntervalTextView = (TextView) findViewById(R.id.intervalTextView);
        mRSSITextView = (TextView) findViewById(R.id.RSSITextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);

        mTransCoord = new TransCoordinate();
        mDBManager = new DBManager();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);

        if(mapHeight != 0)
            return;
        mapHeight = mapImageView.getHeight();
        mapWidth = mapImageView.getWidth();
        Log.w("MAP", "" + mapHeight + "," + mapWidth);

        mTransCoord.setMapSize(mapWidth, mapHeight);
        mBlockSize = mTransCoord.getBlockSize();
        new BackgroundTask().execute();
    }

    class BackgroundTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            View.OnClickListener ocl = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIDTextView.setText("ID: "+(v.getId()+1));

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
            int i,j,cnt=0,point[];
            buttonView = new Vector<ImageView>();
            for(i=0; i<TransCoordinate.MAP_VERTICAL_BLOCK; i++) {
                for(j=0; j<TransCoordinate.MAP_HORIZONTAL_BLOCK; j++) {
                    ImageView imageView = new ImageView(MapUpdateActivity.this);
                    imageView.setImageDrawable(grayButton);
                    imageView.setId(cnt++);
                    imageView.setImageAlpha(50);

                    point = mTransCoord.getPixelPoint(j,i);

                    param = new RelativeLayout.LayoutParams(mBlockSize,mBlockSize);
                    param.leftMargin = point[0];
                    param.topMargin = point[1];
                    imageView.setLayoutParams(param);

                    imageView.setClickable(true);
                    imageView.setOnClickListener(ocl);
                    mapLayout.addView(imageView);
                    buttonView.add(imageView);

                    Log.i("MAP","id: "+(cnt-1)+",x: "+point[0]+",y: "+point[1]);
                }
            }
        }
    }

    // Start Measurement Button
    public void onButtonClicked(View v) {
        Button btn = (Button)v;
        if(btn.getId() == R.id.startMeasurementButton) {
            Log.i("MAP","start measurement!");

            final Intent intent = new Intent(MapUpdateActivity.this, ConfidenceIntervalActivity.class);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int i,j;
        double[] result = null;
        if(resultCode == RESULT_OK && requestCode == 1)
        {
            result = data.getDoubleArrayExtra("rssi_avg");
            mIntervalTextView.setText("Interval: "+(int)result[0]);

            StringBuffer sb = new StringBuffer();
            for(i=0; i<ConfidenceIntervalActivity.WINDOW_SIZE; i++) {
                sb.append("count :");
                sb.append(i);
                sb.append(",   ");
                for(j=1; j<=4; j++)
                {
                    sb.append(j);
                    sb.append(":");
                    sb.append(result[i*4+j]);
                    sb.append(", ");
                }
                sb.append("\n");
            }
            mRSSITextView.setText(sb.toString());
        }

        mDBManager.setTextView(mStatusTextView);
        mDBManager.insertFingerprint(result, checkedButton+1, (int)result[0]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
