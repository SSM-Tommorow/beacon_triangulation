package com.example.hyeonseob.beacontriangulation.Class;

import android.util.Log;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class LocationEstimation {
    private final static int WINDOW_SIZE = 2;
    private SparseArray<int[]> mAdjacentNode;
    private int[][][] mFingerprint;
    private int[][] trans = {{5,62},{11,62},{14,62},{18,62},{21,63},{26,63},{30,63},{37,63},{41,63},{46,63},{49,63},{54,64},{55,72},{61,79},
            {43,61},{44,58},{45,54},{45,51},{45,47},{45,44},{44,40}, {44,37},{44,33},{44,29},{44,25},{44,22},{44,18},{44,14},{44,8},
            {19,26},{16,31},{19,34}};
    private int[][] mBeaconFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private double[] mRSSIAvg;
    private int[] mResult;

    private double sum, min;
    private int count, minloc, i,j,temp;
    private int mPrevLoc, mMeasureCount, mDirection;

    public LocationEstimation(){
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

    public int[] getLocation(Vector<Beacon> beaconList, int direction)
    {
        count = minloc = 0;

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
            for (i = 0; i < 15; i++) {
                temp = 0;
                for (Beacon beacon : beaconList) {
                    if (beacon.getId() == i + 1)
                        temp = beacon.getRSSI();
                }
                if (temp == 0)
                    temp = -100;
                mRSSIAvg[i] += temp;
            }

            if(mPrevLoc == -1)
                return mResult;

            mResult[0] = trans[mPrevLoc][0];
            mResult[1] = trans[mPrevLoc][1];
            mResult[2] = (int)min;
            return mResult;
        }

        for (i = 0; i < 15; i++) {
            temp = 0;
            for (Beacon beacon : beaconList) {
                if (beacon.getId() == i + 1)
                    temp = beacon.getRSSI();
            }

            if (temp == 0)
                temp = -100;
            mRSSIAvg[i] += temp;
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

        min = 999999999;
        for(i=0; i<32; i++) {
            sum = 0;
            for(j=0; j<15; j++){
                sum += Math.pow(Math.pow(mFingerprint[i][mDirection][j], 2) - Math.pow(mRSSIAvg[j], 2),2);
            }
            sum = Math.sqrt(sum);
            if (sum < min) {
                min = sum;
                minloc = i;
            }
        }

        mPrevLoc = minloc;
        mMeasureCount = 0;
        mRSSIAvg = new double[15];

        mResult[0] = trans[mPrevLoc][0];
        mResult[1] = trans[mPrevLoc][1];
        mResult[2] = (int)min;

        Log.i("MAP","Estimated: ("+mResult[0]+","+mResult[1]+"), "+mResult[2]);
        return mResult;
    }
}
