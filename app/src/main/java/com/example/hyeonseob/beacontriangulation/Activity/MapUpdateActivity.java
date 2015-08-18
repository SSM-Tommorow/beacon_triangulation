package com.example.hyeonseob.beacontriangulation.Activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Class.DBManager;
import com.example.hyeonseob.beacontriangulation.R;
import com.example.hyeonseob.beacontriangulation.Class.TransCoordinate;
import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;

public class MapUpdateActivity extends RECOActivity implements RECORangingListener {
    public final static int WINDOW_SIZE = 20;
    private int mapHeight, mapWidth, mBlockWidth, mBlockHeight;

    private ImageView mCircleView;
    private float mX, mY, mDX, mDY;
    private int mMajor, mMinor;
    private StringBuffer mStrBuff;
    private RelativeLayout mapLayout;
    private TextView mIDTextView, mRSSITextView, mStatusTextView;
    private ImageView mapImageView;
    private Drawable redButton, grayButton, blueButton;

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
        mIDTextView = (TextView) findViewById(R.id.idTextView);
        mRSSITextView = (TextView) findViewById(R.id.RSSITextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);

        mStrBuff = new StringBuffer();
        mCircleView = new ImageView(this);
        mCircleView.setImageDrawable(grayButton);
        mapLayout.addView(mCircleView);
        mapLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mX = event.getX();
                        mY = event.getY();
                        mDX = mX-mCircleView.getX();
                        mDY = mY-mCircleView.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCircleView.setX(event.getX() - mDX);
                        mCircleView.setY(event.getY()-mDY);
                        break;
                    case MotionEvent.ACTION_UP:
                        mX = mCircleView.getX();
                        mY = mCircleView.getY();
                        int[] point = mTransCoord.getCoordinate(mX, mY);
                        mIDTextView.setText("ID: "+point[0] + ", X: "+(point[1]+1)+", Y: "+(point[2]+1));
                        break;
                }
                return true;
            }
        });

        mTransCoord = new TransCoordinate();
        mDBManager = new DBManager();

        mapImageView.post(new Runnable() {
            @Override
            public void run() {
                if(mapHeight != 0)
                    return;

                mapHeight = mapImageView.getMeasuredHeight();
                mapWidth = mapImageView.getMeasuredWidth();
                Log.w("MAP", "" + mapHeight + "," + mapWidth);

                mTransCoord.setMapSize(mapWidth, mapHeight);
                mBlockWidth = mTransCoord.getBlockWidth();
                mBlockHeight = mTransCoord.getBLockHeight();

                mCircleView.setLayoutParams(new RelativeLayout.LayoutParams(mBlockWidth*2, mBlockHeight*2));
            }
        });
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoBeaconRegion.getUniqueIdentifier() + ", number of beacons ranged: " + collection.size());

        mStrBuff.setLength(0);
        mStrBuff.append("RSSI:\n");
        for(RECOBeacon beacon : collection)
        {
            mMajor = beacon.getMajor()-1;
            mMinor = beacon.getMinor()-1;
            mStrBuff.append("Major: ");
            mStrBuff.append(mMajor + 1);
            mStrBuff.append(", Minor: ");
            mStrBuff.append(mMinor+1);
            mStrBuff.append(", RSSI: ");
            mStrBuff.append(beacon.getRssi());
            mStrBuff.append("\n");
        }
        mRSSITextView.setText(mStrBuff.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int[] result = null;
        if(resultCode == RESULT_OK)
        {
            result = data.getIntArrayExtra("rssi_list");

            StringBuffer sb = new StringBuffer();
            for(int a : result)
            {
                sb.append(a);
                sb.append(", ");
            }
            mRSSITextView.setText(sb.toString());

            mDBManager.setTextView(mStatusTextView);
            if(requestCode == 1)
            {
                //mDBManager.insertFingerprint(result, checkedButton+1, (int)result[0]);
            }
            else if(requestCode == 2)
            {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, Menu.NONE, "Update RSSI");
        menu.add(0, 1, Menu.NONE, "Start Estimation");
        menu.add(0, 2, Menu.NONE, "Stop Estimation");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == 0) {
            final Intent intent = new Intent(MapUpdateActivity.this, ConfidenceIntervalActivity.class);
            startActivityForResult(intent, 1);
        }
        else if(id == 1) {
            mRecoManager.setRangingListener(this);
            mRecoManager.bind(this);
        }
        else if(id == 2) {
            this.stop(mRegions);
            this.unbind();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnect() {
        Log.i("RECORangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(MainActivity.DISCONTINUOUS_SCAN);
        this.start(mRegions);
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {}

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stop(mRegions);
        this.unbind();
    }

    private void unbind() {
        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.i("RECORangingActivity", "Remote Exception");
            e.printStackTrace();
        }
    }

    @Override
    protected void start(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void stop(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }
}
