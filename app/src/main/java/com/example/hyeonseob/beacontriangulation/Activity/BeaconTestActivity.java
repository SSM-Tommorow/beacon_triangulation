package com.example.hyeonseob.beacontriangulation.Activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Intro.MainActivity;
import com.example.hyeonseob.beacontriangulation.R;
import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;

public class BeaconTestActivity extends RECOActivity implements RECORangingListener {
    private double[] mAccuracy;
    private int[] mRSSI;
    private StringBuffer mStrBuff;
    private TextView mTextView;
    private int mCount, i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_test);

        mTextView = (TextView) findViewById(R.id.statusTextView);
        mStrBuff = new StringBuffer();

        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        mAccuracy = new double[15];
        mRSSI = new int[15];
        mCount = 1;
        try {
            for (RECOBeacon beacon : collection) {
                mAccuracy[(beacon.getMajor()-1)*3+beacon.getMinor()-1] = beacon.getAccuracy();
                mRSSI[(beacon.getMajor()-1)*3+beacon.getMinor()-1] = beacon.getRssi();
            }
        } catch(Exception e){
            e.printStackTrace();
            return;
        }

        mStrBuff.setLength(0);
        for(i=0; i<15; i++){
            mStrBuff.append("[").append(i/3+1).append(",").append(i%3+1).append("] : ");
            if(mRSSI[i] == 0)
                mStrBuff.append("X\n");
            else
                mStrBuff.append(mRSSI[i]).append(" (").append(((int)(mAccuracy[i]*10))/10.0).append("m)\n");
        }
        mTextView.setText(mStrBuff.toString());
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {}

    @Override
    public void onServiceConnect() {
        mRecoManager.setDiscontinuousScan(MainActivity.DISCONTINUOUS_SCAN);
        this.start(mRegions);
    }

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
