package com.example.hyeonseob.beacontriangulation.Activity;

import android.content.Intent;
import android.os.RemoteException;
import android.os.Bundle;
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
import java.util.Vector;

public class ConfidenceIntervalActivity extends RECOActivity implements RECORangingListener {

    public final static int WINDOW_SIZE = 20;
    private TextView mDetectedBeaconTextView;
    private TextView mCountTextView;

    private int[][] mBeaconFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private int mCount;
    private Vector<Integer> mRSSIVect;
    private StringBuffer mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confidence_interval);

        mResult = new StringBuffer();
        mCount = 0;
        mRSSIVect = new Vector<>();

        mDetectedBeaconTextView = (TextView) findViewById(R.id.detectedBeaconTextView);
        mCountTextView = (TextView) findViewById(R.id.countTextView);

        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoBeaconRegion.getUniqueIdentifier() + ", number of beacons ranged: " + collection.size());
        int[] mRSSI = new int[15];
        int[][] tempFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
        int count=0, major, minor, i, j;
        mResult.setLength(0);

        for(RECOBeacon beacon : collection)
        {
            major = beacon.getMajor()-1;
            minor = beacon.getMinor()-1;
            tempFlag[major][minor] = 1;

            if(mBeaconFlag[major][minor] == 0)
            {
                mBeaconFlag[major][minor] = 1;
                continue;
            }
            else {
                mBeaconFlag[major][minor] = 2;
                count++;
                mResult.append("Major: ");
                mResult.append(major+1);
                mResult.append(", Minor: ");
                mResult.append(minor+1);
                mResult.append(", RSSI: ");
                mResult.append(beacon.getRssi());
                mResult.append("\n");

                mRSSI[major*3 + minor + 1] = beacon.getRssi();
            }
        }

        String temp = new String();
        for(i=0; i<5; i++) {
            for (j = 0; j < 3; j++) {
                temp += "(" + i + "," + j + ") : " + mBeaconFlag[i][j] + ", ";
                if (tempFlag[i][j] == 0)
                    mBeaconFlag[i][j] = 0;
            }
        }
        Log.i("MAP","flag: "+temp+", count: "+count);

        if(count > 0) {
            for (i = 0; i < 15; i++)
                mRSSIVect.add(mRSSI[i]);
            mCount++;
        }

        mDetectedBeaconTextView.setText(mResult.toString());
        mCountTextView.setText("Count: "+ mCount);

        if(mCount >= WINDOW_SIZE)
        {
            int[] result = new int[mRSSIVect.size()];
            for(i=1; i<mRSSIVect.size(); i++)
                result[i] = mRSSIVect.elementAt(i);
            result[0] = 0;

            Intent intent = new Intent();
            intent.putExtra("rssi_list",result);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i("RECORangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(MainActivity.DISCONTINUOUS_SCAN);
        this.start(mRegions);
        //Write the code when RECOBeaconManager is bound to RECOBeaconService
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

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {
        //Write the code when the RECOBeaconService is failed.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {
        //Write the code when the RECOBeaconService is failed to range beacons in the region.
        //See the RECOErrorCode in the documents.
        return;
    }

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
}
