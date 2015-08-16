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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class MapUpdateActivity extends Activity implements OnTaskCompleted{
    private int mapHeight, mapWidth, mBlockSize;
    private int checkedButton = -1;

    private RelativeLayout mapLayout;
    private ImageView mapImageView;
    private Vector<ImageView> buttonView;
    private TextView mXTextView, mYTextView, mIDTextView, mIntervalTextView, mRSSITextView, mStatusTextView;

    private Drawable redButton;
    private Drawable grayButton;
    private Drawable blueButton;

    private TransCoordinate mTransCoord;
    private DBManager mDBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_update);

        redButton = getResources().getDrawable(R.drawable.red_button);
        grayButton = getResources().getDrawable(R.drawable.gray_button);
        blueButton = getResources().getDrawable(R.drawable.blue_button);
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

    @Override
    public void onTaskCompleted(JSONObject jsonObj) {
        JSONObject jsonArr;
        ImageView imageView;
        int buttonID;
        try {
            for(int i=0; i<5; i++)
            {
                jsonArr = (JSONObject)jsonObj.get(""+i);
                buttonID = Integer.parseInt(jsonArr.getString("coordinate_id"));
                imageView = buttonView.get(buttonID - 1);

                Log.i("MAP","button ID: "+buttonID+", alpha: "+imageView.getAlpha());
                imageView.setImageAlpha(imageView.getImageAlpha()+(5-i)*20);
                imageView.setImageDrawable(blueButton);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    mXTextView.setText("X: "+(v.getId()%14+1)+", ");
                    mYTextView.setText("Y: "+(v.getId()/14+1)+", ");

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
        if(btn.getId() == R.id.updateRSSIButton) {
            Log.i("MAP","start measurement!");

            final Intent intent = new Intent(MapUpdateActivity.this, ConfidenceIntervalActivity.class);
            startActivityForResult(intent, 1);
        }
        else if(btn.getId() == R.id.estimationButton) {
            Log.i("MAP","start estimation");

            final Intent intent = new Intent(MapUpdateActivity.this, ConfidenceIntervalActivity.class);
            startActivityForResult(intent, 2);
        }
        else if(btn.getId() == R.id.showRSSIButton) {
            Log.i("MAP","start searching RSSI");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int i,j;
        double[] result = null;
        if(resultCode == RESULT_OK)
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
            mDBManager.setTextView(mStatusTextView);

            if(requestCode == 1)
            {

                mDBManager.insertFingerprint(result, checkedButton+1, (int)result[0]);
            }
            else if(requestCode == 2)
            {
                for(ImageView b : buttonView)
                {
                    b.setImageDrawable(grayButton);
                    b.setImageAlpha(50);
                }

                for(i=0; i<10; i++)
                {
                    double[] temp = {0,0,0,0,0};
                    temp[0] = result[0];
                    temp[1] = result[i*4+1];
                    temp[2] = result[i*4+2];
                    temp[3] = result[i*4+3];
                    temp[4] = result[i*4+4];
                    Log.i("MAP","searching: "+temp[0]+","+temp[1]+","+temp[2]+","+temp[3]+","+temp[4]);
                    mDBManager.getEstimatedLocation(temp, this);
                }
            }
        }
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
