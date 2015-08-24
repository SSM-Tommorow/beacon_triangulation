package com.example.hyeonseob.beacontriangulation.Class;

import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Vector;

public class LocationEstimation {
    private SparseArray<int[]> mAdjacentNode;
    private int[][][] mFingerprint;
    private int[][] trans = {{5,62},{8,62},{12,62},{16,62},{20,63},{24,63},{28,63},{32,63},{37,63},{42,63},{46,63},{49,63},{53,73},{60,79},
            {43,59},{43,56},{44,52},{44,49},{44,45},{44,42},{44,38},{44,35},{44,31},{44,27},{44,24},{44,19},{44,14},{44,10},{44,5},{16,26},{16,29},{16,33}};
    private int[][] mBeaconFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};

    private double sum, min;
    private int count, minloc, major, minor,i,j;
    private int mPrevLoc;

    public LocationEstimation(){
        mPrevLoc = -1;
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
    }

    public void setFingerprint(int[][][] fingerprint) {
        mFingerprint = fingerprint;
    }

    public int[] getLocation(Vector<Beacon> beaconList, int direction)
    {
        min = 999999999;
        count = minloc = 0;

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
            for (j = 0; j < 3; j++)
                if (tempFlag[i][j] == 0)
                    mBeaconFlag[i][j] = 0;


        // Search only adjacent location
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
                    sum += Math.pow(Math.pow(mFingerprint[i][direction][(major - 1) * 3 + minor - 1], 2) - Math.pow(beacon.getRSSI(), 2), 2);
                }
                if (sum < min) {
                    min = sum;
                    minloc = i;
                }
            }
        }
        Log.i("MAP","Estimated: ("+trans[minloc][0]+","+trans[minloc][1]+")");

        if(mPrevLoc == -1)
            mPrevLoc = minloc;
        return trans[minloc];
    }
}
