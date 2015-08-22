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

import com.example.hyeonseob.beacontriangulation.Class.Beacon;
import com.example.hyeonseob.beacontriangulation.Class.DBManager;
import com.example.hyeonseob.beacontriangulation.Class.LocationEstimation;
import com.example.hyeonseob.beacontriangulation.R;
import com.example.hyeonseob.beacontriangulation.Class.TransCoordinate;
import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class MapUpdateActivity extends RECOActivity implements RECORangingListener {
    public final static int WINDOW_SIZE = 20;
    private final static int INITIAL_STATE = 0, UPDATE_STATE = 1, ESTIMATION_STATE = 2;

    private int mCurrentState = INITIAL_STATE;
    private float mX, mY, mDX, mDY;
    private int mMajor, mMinor, mRSSI;
    private int mMapHeight, mMapWidth, mBlockWidth, mBlockHeight;
    private Vector<Beacon> mBeaconList;

    private ImageView mCircleView, mMapImageView;
    private StringBuffer mStrBuff;
    private RelativeLayout mMapLayout;
    private TextView mIDTextView, mRSSITextView, mStatusTextView;
    private Drawable mRedButton, mGrayButton, mBlueButton;

    private LocationEstimation mLocEst;
    private TransCoordinate mTransCoord;
    private DBManager mDBManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_update);

        mRedButton = getResources().getDrawable(R.drawable.red_button);
        mGrayButton = getResources().getDrawable(R.drawable.gray_button);
        mBlueButton = getResources().getDrawable(R.drawable.blue_button);
        mMapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        mMapImageView = (ImageView) findViewById(R.id.mapImageView);
        mIDTextView = (TextView) findViewById(R.id.idTextView);
        mRSSITextView = (TextView) findViewById(R.id.RSSITextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);

        mStrBuff = new StringBuffer();
        mCircleView = new ImageView(this);
        mCircleView.setImageDrawable(mGrayButton);
        mMapLayout.addView(mCircleView);
        mMapLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mX = event.getX();
                        mY = event.getY();
                        mDX = mX - mCircleView.getX();
                        mDY = mY - mCircleView.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCircleView.setX(event.getX() - mDX);
                        mCircleView.setY(event.getY() - mDY);
                        break;
                    case MotionEvent.ACTION_UP:
                        mX = mCircleView.getX();
                        mY = mCircleView.getY();
                        int[] point = mTransCoord.getCoordinate(mX, mY);
                        mIDTextView.setText("ID: " + point[0] + ", X: " + (point[1] + 1) + ", Y: " + (point[2] + 1));
                        break;
                }
                return true;
            }
        });

        mTransCoord = new TransCoordinate();
        mDBManager = new DBManager();
        mLocEst = new LocationEstimation();

        mMapImageView.post(new Runnable() {
            @Override
            public void run() {
                if (mMapHeight != 0)
                    return;

                mMapHeight = mMapImageView.getMeasuredHeight();
                mMapWidth = mMapImageView.getMeasuredWidth();
                Log.w("MAP", "" + mMapHeight + "," + mMapWidth);

                mTransCoord.setMapSize(mMapWidth, mMapHeight);
                mBlockWidth = mTransCoord.getBlockWidth();
                mBlockHeight = mTransCoord.getBLockHeight();

                mCircleView.setLayoutParams(new RelativeLayout.LayoutParams(mBlockWidth * 2, mBlockHeight * 2));
            }
        });
    }


    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoBeaconRegion.getUniqueIdentifier() + ", number of beacons ranged: " + collection.size());
        mBeaconList = new Vector<>();
        mStrBuff.setLength(0);
        mStrBuff.append("RSSI:\n");
        for(RECOBeacon beacon : collection)
        {
            mMajor = beacon.getMajor();
            mMinor = beacon.getMinor();
            mRSSI = beacon.getRssi();

            mStrBuff.append("BeaconID: (");
            mStrBuff.append(mMajor);
            mStrBuff.append(",");
            mStrBuff.append(mMinor);
            mStrBuff.append("), RSSI: ");
            mStrBuff.append(mRSSI);
            mStrBuff.append("\n");

            mBeaconList.add(new Beacon((mMajor-1)*3+mMinor,mMajor,mMinor,mRSSI));
        }
        mRSSITextView.setText(mStrBuff.toString());

        mDBManager.setTextView(mStatusTextView);
        if(mCurrentState == UPDATE_STATE)
        {

        }
        else if(mCurrentState == ESTIMATION_STATE)
        {
            int[] point = mLocEst.getLocation(mBeaconList);
            mStatusTextView.setText("Estimated : ("+point[0]+","+point[1]+")");

            point = mTransCoord.getPixelPoint(point[0], point[1]);
            mCircleView.setX(point[0]);
            mCircleView.setY(point[1]);
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
            mStatusTextView.setText("Sataus: Start updating");
            mRecoManager.setRangingListener(this);
            mRecoManager.bind(this);
            mBeaconList = new Vector<>();
        }
        else if(id == 1) {
            mStatusTextView.setText("Status: Start estimating");
            mCurrentState = ESTIMATION_STATE;
            mRecoManager.setRangingListener(this);
            mRecoManager.bind(this);
        }
        else if(id == 2) {
            mStatusTextView.setText("Status: Stop estimating");
            mCurrentState = INITIAL_STATE;
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
