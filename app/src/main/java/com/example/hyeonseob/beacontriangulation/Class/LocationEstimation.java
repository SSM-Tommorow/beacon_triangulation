package com.example.hyeonseob.beacontriangulation.Class;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.hyeonseob.beacontriangulation.Activity.NavigationActivity;

import java.util.Vector;

public class LocationEstimation {
    private final static int INTERVAL = 20000000;
    private final static int INTERVAL_1_1 = 5000000, INTERVAL_1_2 = 7000000, INTERVAL_1_3 = 10000000, INTERVAL_1_4 = 10000000, INTERVAL_1_5 = 6700000, INTERVAL_1_6 = 7000000;
    private final static int INTERVAL_2_1 = 6000000, INTERVAL_2_2 = 7000000, INTERVAL_2_3 = 9000000, INTERVAL_2_4 = 9000000, INTERVAL_2_5 = 7000000, INTERVAL_2_6 = 6000000;
    private final static int INTERVAL_3_1 = 5000000, INTERVAL_3_2 = 7000000, INTERVAL_3_3 = 9000000, INTERVAL_3_4 = 9000000, INTERVAL_3_5 = 6700000, INTERVAL_3_6 = 6000000;
    private final static int INTERVAL_4_1 = 6000000, INTERVAL_4_2 = 7000000, INTERVAL_4_3 = 9000000, INTERVAL_4_4 = 9000000, INTERVAL_4_5 = 7000000, INTERVAL_4_6 = 6000000;
    private final static int WINDOW_SIZE = 2;
    private final static int[][] TRANS = {{5,62},{11,62},{14,62},{18,62},{21,63},{26,63},{30,63},{37,63},{41,63},{46,63},{49,63},{54,64},{55,72},{61,79},
            {43,61},{44,58},{45,54},{45,51},{45,47},{45,44},{44,40}, {44,37},{44,33},{44,29},{44,25},{44,22},{44,18},{44,14},{44,8},
            {23,26},{16,31},{23,34}};
    private final static int[][] RSSI_CONFIDENCE = {
            {INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_1,
                    INTERVAL_1_2, INTERVAL_1_2, INTERVAL_1_2, INTERVAL_1_2,
                    INTERVAL_1_3, INTERVAL_1_3, INTERVAL_1_3,
                    INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4,
                    INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5,
                    INTERVAL_1_6, INTERVAL_1_6, INTERVAL_1_6},


            {INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_1,
                    INTERVAL_2_2, INTERVAL_2_2, INTERVAL_2_2, INTERVAL_2_2,
                    INTERVAL_2_3, INTERVAL_2_3, INTERVAL_2_3,
                    INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4,
                    INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5,
                    INTERVAL_2_6, INTERVAL_2_6, INTERVAL_2_6},

            {INTERVAL_3_1, INTERVAL_3_1, INTERVAL_3_1, INTERVAL_3_1, INTERVAL_3_1,
                    INTERVAL_3_2, INTERVAL_3_2, INTERVAL_3_2, INTERVAL_3_2,
                    INTERVAL_3_3, INTERVAL_3_3, INTERVAL_3_3,
                    INTERVAL_3_4, INTERVAL_3_4, INTERVAL_3_4,
                    INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5, INTERVAL_3_5,
                    INTERVAL_3_6, INTERVAL_3_6, INTERVAL_3_6},

            {INTERVAL_4_1, INTERVAL_4_1, INTERVAL_4_1, INTERVAL_4_1, INTERVAL_4_1,
                    INTERVAL_4_2, INTERVAL_4_2, INTERVAL_4_2, INTERVAL_4_2,
                    INTERVAL_4_3, INTERVAL_4_3, INTERVAL_4_3,
                    INTERVAL_4_4, INTERVAL_4_4, INTERVAL_4_4,
                    INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5, INTERVAL_4_5,
                    INTERVAL_4_6, INTERVAL_4_6, INTERVAL_4_6}
            };

    private int[][][] mFingerprint;
    private boolean[] mBeaconFlag;
    private double[] mRSSIAvg;
    private int[] mResult;
    private int mDeviceNum;

    private double sum, min, mActualMin;
    private int count, minloc, mActualMinLoc, i, j, temp;
    private int mPrevLoc, mMeasureCount, mDirection;

    public LocationEstimation(int deviceNum){
        mDeviceNum = deviceNum;
        mPrevLoc = -1;
        mMeasureCount = mDirection = 0;
        mRSSIAvg = new double[15];
        mResult = new int[3];
    }

    public void setFingerprint(int[][][] fingerprint) {
        mFingerprint = fingerprint;
    }
    public int[] getLocation(Vector<Beacon> beaconList, int direction) {
        if(direction <= 45)
            mDirection = 3;
        else if(direction <= 135)
            mDirection = 0;
        else if(direction <= 225)
            mDirection = 1;
        else if(direction <= 315)
            mDirection = 2;
        else
            mDirection = 3;

        // WindowSize
        if(++mMeasureCount < WINDOW_SIZE) {
            mBeaconFlag = new boolean[15];
            for (Beacon beacon : beaconList) {
                mRSSIAvg[beacon.getId()-1] += beacon.getRSSI();
                mBeaconFlag[beacon.getId()-1] = true;
            }
            for (i = 0; i < 15; i++) {
                if(!mBeaconFlag[i])
                    mRSSIAvg[i] += -100;
            }
            return null;
        }

        mBeaconFlag = new boolean[15];
        for (Beacon beacon : beaconList) {
            mRSSIAvg[beacon.getId()-1] += beacon.getRSSI();
            mBeaconFlag[beacon.getId()-1] = true;
        }

        for (i = 0; i < 15; i++) {
            if(!mBeaconFlag[i])
                mRSSIAvg[i] += -100;
            mRSSIAvg[i] /= WINDOW_SIZE;
        }

        count = minloc = mActualMinLoc = 0;
        min = mActualMin = 999999999;
        for(i=0; i<32; i++) {
            if(i==14)
                continue;

            sum = 0;
            for(j=0; j<15; j++){
                sum += Math.pow(Math.pow(mFingerprint[i][mDirection][j], 2) - Math.pow(mRSSIAvg[j], 2),2);
            }
            if (sum < min /*&& !isBehind(i, mDirection)*/ && sum <= RSSI_CONFIDENCE[mDeviceNum][i]) {
                min = sum;
                minloc = i;
            }
        }
        Log.i("LOC","min: "+min+", minloc: "+minloc);

        if((mPrevLoc ==  minloc) || (min == 999999999))
            return null;

        mPrevLoc = minloc;
        mMeasureCount = 0;
        mRSSIAvg = new double[15];

        mResult[0] = TRANS[mPrevLoc][0];
        mResult[1] = TRANS[mPrevLoc][1];
        mResult[2] = (int)min;

        Log.i("MAP","Estimated: ("+mResult[0]+","+mResult[1]+"), "+mResult[2]);
        return mResult;
    }

    private boolean isBehind(int currentLoc, int direction){
        if(mPrevLoc == -1)
            return false;

        switch(direction){
            case 0:
                if(TRANS[mPrevLoc][1] < TRANS[currentLoc][1])
                    return true;
                break;
            case 1:
                if(TRANS[mPrevLoc][0] > TRANS[currentLoc][0])
                    return true;
                break;
            case 2:
                if(TRANS[mPrevLoc][1] > TRANS[currentLoc][1])
                    return true;
                break;
            case 3:
                if(TRANS[mPrevLoc][0] < TRANS[currentLoc][0])
                    return true;
                break;
        }
        return false;
    }

    private boolean isFront(int currentLoc, int direction){
        if(mPrevLoc == -1)
            return true;

        switch(direction){
            case 0:
                if(TRANS[mPrevLoc][1] > TRANS[currentLoc][1])
                    return true;
                break;
            case 1:
                if(TRANS[mPrevLoc][0] < TRANS[currentLoc][0])
                    return true;
                break;
            case 2:
                if(TRANS[mPrevLoc][1] < TRANS[currentLoc][1])
                    return true;
                break;
            case 3:
                if(TRANS[mPrevLoc][0] > TRANS[currentLoc][0])
                    return true;
                break;
        }
        return false;
    }
}
