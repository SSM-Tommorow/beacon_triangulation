package com.example.hyeonseob.beacontriangulation.Class;

import android.util.Log;
import android.util.SparseArray;

import java.util.Vector;

public class LocationEstimation {
    private final static int INTERVAL = 20000000;
    private final static int INTERVAL_1_1 = 5000000, INTERVAL_1_2 = 7000000, INTERVAL_1_3 = 12000000, INTERVAL_1_4 = 7000000, INTERVAL_1_5 = 13000000;
    private final static int INTERVAL_2_1 = 6000000, INTERVAL_2_2 = 7000000, INTERVAL_2_3 = 8000000, INTERVAL_2_4 = 7000000, INTERVAL_2_5 = 6000000;
    private final static int WINDOW_SIZE = 2;
    private final static int[][] TRANS = {{5,62},{11,62},{14,62},{18,62},{21,63},{26,63},{30,63},{37,63},{41,63},{46,63},{49,63},{54,64},{55,72},{61,79},
            {43,61},{44,58},{45,54},{45,51},{45,47},{45,44},{44,40}, {44,37},{44,33},{44,29},{44,25},{44,22},{44,18},{44,14},{44,8},
            {19,26},{16,31},{19,34}};
    private final static int[][] RSSI_CONFIDENCE = {
            {INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_1, INTERVAL_1_2, INTERVAL_1_2, INTERVAL_1_2, INTERVAL_1_2, INTERVAL_1_3, INTERVAL_1_3, INTERVAL_1_3, INTERVAL_1_3, INTERVAL_1_3, INTERVAL_1_3,
                    INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4, INTERVAL_1_4,
                    INTERVAL_1_5, INTERVAL_1_5, INTERVAL_1_5},

            {INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_1, INTERVAL_2_2, INTERVAL_2_2, INTERVAL_2_2, INTERVAL_2_2, INTERVAL_2_3, INTERVAL_2_3, INTERVAL_2_3, INTERVAL_2_3, INTERVAL_2_3, INTERVAL_2_3,
                    INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4, INTERVAL_2_4,
                    INTERVAL_2_5, INTERVAL_2_5, INTERVAL_2_5},

            {INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL, INTERVAL,INTERVAL,INTERVAL,INTERVAL, INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,
                    INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL, INTERVAL,INTERVAL,INTERVAL},

            {INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL, INTERVAL,INTERVAL,INTERVAL,INTERVAL, INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,
                    INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL,INTERVAL, INTERVAL,INTERVAL,INTERVAL}
            };

    private SparseArray<int[]> mAdjacentNode;
    private int[][][] mFingerprint;
    private boolean[] mBeaconFlag;
    private double[] mRSSIAvg;
    private int[] mResult;
    private int mDeviceNum;

    private double sum, min;
    private int count, minloc, i, j, temp;
    private int mPrevLoc, mMeasureCount, mDirection;

    public LocationEstimation(int deviceNum){
        mDeviceNum = deviceNum;
        mPrevLoc = -1;
        mMeasureCount = mDirection = 0;
        mRSSIAvg = new double[15];
        mResult = new int[3];

        /*
        mAdjacentNode = new SparseArray<>();
        mAdjacentNode.append(0,new int[]{0,1});
        mAdjacentNode.append(1,new int[]{0,1,2});
        mAdjacentNode.append(2,new int[]{1,2,3});
        mAdjacentNode.append(3,new int[]{2,3,4});
        mAdjacentNode.append(4,new int[]{3,4,5});
        mAdjacentNode.append(5,new int[]{4,5,6});
        mAdjacentNode.append(6,new int[]{5,6,7});
        mAdjacentNode.append(7,new int[]{6,7,8});
        mAdjacentNode.append(8,new int[]{7,8,9,14});
        mAdjacentNode.append(9,new int[]{8,9,10,14});
        mAdjacentNode.append(10,new int[]{9,10,11,14});
        mAdjacentNode.append(11,new int[]{10,11,12});
        mAdjacentNode.append(12,new int[]{11,12,13});
        mAdjacentNode.append(13,new int[]{12,13});
        */
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

        // Ignore the first rssi value
        /*
        int[][] tempFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
        for(i=beaconList.size()-1; i>=0; i--)
        {
            major = beaconList.get(i).getMajor()-1;
            minor = beaconList.get(i).getMinor()-1;
            tempFlag[major][minor] = 1;
            if(mBeaconFlag[major][minor] == 0)
            {
                mBeaconFlag[major][minor] = 1;
                beaconList.remove(i);
                continue;
            }
            mBeaconFlag[major][minor] = 2;
        }

        for(i=0; i<5; i++)
        {
            for (j = 0; j < 3; j++)
                if (tempFlag[i][j] == 0)
                    mBeaconFlag[i][j] = 0;
        }
        */


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

        // Search only adjacent location
        /*
        if(mPrevLoc != -1) {
            for(int index : mAdjacentNode.get(mPrevLoc)) {
                sum = 0;
                for(Beacon beacon : beaconList) {
                    major = beacon.getMajor();
                    minor = beacon.getMinor();
                    sum += Math.pow(Math.pow(mFingerprint[index][direction][(major-1)*3+minor-1], 2) - Math.pow(beacon.getRSSI(), 2), 2);
                }
                if(sum < min) {
                    min = sum;
                    minloc = index;
                }
            }
        }
        else {
            for(i=0; i<32; i++) {
                sum = 0;
                for (Beacon beacon : beaconList) {
                    major = beacon.getMajor();
                    minor = beacon.getMinor();
                    // Temporary
                    if(i>13)
                        direction = 0;
                    else
                        direction = 1;
                    sum += Math.pow(Math.pow(mFingerprint[i][direction][(major - 1) * 3 + minor - 1], 2) - Math.pow(beacon.getRSSI(), 2), 2);
                }
                if (sum < min) {
                    min = sum;
                    minloc = i;
                }
            }
        }
        */

        count = minloc = 0;
        min = 999999999;
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
