package com.example.hyeonseob.beacontriangulation;

import android.content.Intent;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class ConfidenceIntervalActivity extends RECOActivity implements RECORangingListener {
    final static int WINDOW_SIZE = 100, RSSI_TRHESHOLD = -99999;
    private TextView mIntervalTextView;
    private TextView mDetectedBeaconTextView;
    private TextView mCountTextView;

    private int mNearestBeaconMajor, mFirstNearestBeaconMajor;
    private int mCount;
    private double[] mRSSIAvg, mRSSI;
    private Vector<Double> mRSSIVect;

    private StringBuffer mInterval;
    private StringBuffer mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confidence_interval);

        mInterval = new StringBuffer();
        mResult = new StringBuffer();
        mCount = 0;
        mRSSI = new double[5];
        mRSSIAvg = new double[5];
        mFirstNearestBeaconMajor = 0;
        mRSSIVect = new Vector<>();
        mRSSIVect.add(0.0);

        mIntervalTextView = (TextView) findViewById(R.id.intervalTextView);
        mDetectedBeaconTextView = (TextView) findViewById(R.id.detectedBeaconTextView);
        mCountTextView = (TextView) findViewById(R.id.countTextView);

        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoBeaconRegion.getUniqueIdentifier() + ", number of beacons ranged: " + collection.size());

        int count=0;
        mInterval.setLength(0);
        mResult.setLength(0);

        mNearestBeaconMajor = collection.iterator().next().getMajor();
        if(mFirstNearestBeaconMajor == 0)
            mFirstNearestBeaconMajor = mNearestBeaconMajor;
        else if(mFirstNearestBeaconMajor != mNearestBeaconMajor)
            return;

        for(RECOBeacon beacon : collection)
        {
            if(beacon.getMajor() == mNearestBeaconMajor && beacon.getRssi() > RSSI_TRHESHOLD) {
                count++;
                mResult.append("Major: ");
                mResult.append(beacon.getMajor());
                mResult.append(", Minor: ");
                mResult.append(beacon.getMinor());
                mResult.append(", RSSI: ");
                mResult.append(beacon.getRssi());
                mResult.append("\n");

                mRSSI[beacon.getMinor()] = beacon.getRssi();
            }
        }

        if((mNearestBeaconMajor == 2 && count > 2) || count > 3) {
            mInterval.append("Interval: ").append(mNearestBeaconMajor);

            for(int i=1; i<5; i++)
                mRSSIVect.add(mRSSI[i]);
            mCount++;
        }
        else
            mInterval.append("Interval: Out of confidence intervals");

        mIntervalTextView.setText(mInterval.toString());
        mDetectedBeaconTextView.setText(mResult.toString());
        mCountTextView.setText("Count: "+ mCount);

        if(mCount >= WINDOW_SIZE)
        {
            double[] result = new double[mRSSIVect.size()];
            for(int i=1; i<mRSSIVect.size(); i++)
                result[i] = mRSSIVect.elementAt(i);
            result[0] = mFirstNearestBeaconMajor;

            Intent intent = new Intent();
            intent.putExtra("rssi_avg",result);

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
