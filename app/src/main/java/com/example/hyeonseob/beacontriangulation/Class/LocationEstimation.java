package com.example.hyeonseob.beacontriangulation.Class;

import android.util.Log;

import java.util.HashMap;
import java.util.Vector;

public class LocationEstimation {
    private Vector<HashMap<Integer, Integer>> mFingerprint;
    private HashMap<Integer, Integer> mMap;
    private int[][] trans = {{5,62},{8,62},{12,62},{16,62},{20,63},{24,63},{28,63},{32,63},{37,63},{42,63},{46,63},{49,63}};
    private int[][] mBeaconFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private int mInterval, mPrevX, mPrevY;


    public LocationEstimation(){
        mFingerprint  = new Vector<>();
        mInterval = mPrevX = mPrevY = 0;

        // before 1-1
        mMap = new HashMap<>();
        mMap.put(11, 67);mMap.put(12, 72);mMap.put(13, 78);
        mMap.put(21, 76);
        mFingerprint.add(mMap);

        // 1-1
        mMap = new HashMap<>();
        mMap.put(11, 60);mMap.put(12, 70);mMap.put(13, 77);
        mMap.put(21, 75);
        mFingerprint.add(mMap);

        // before 1-2
        mMap = new HashMap<>();
        mMap.put(11, 70);mMap.put(12, 63);mMap.put(13, 75);
        mMap.put(21, 74);
        mFingerprint.add(mMap);

        // 1-2
        mMap = new HashMap<>();
        mMap.put(11, 77);mMap.put(12, 57);mMap.put(13, 70);
        mMap.put(21, 71);
        mFingerprint.add(mMap);

        // before 1-3
        mMap = new HashMap<>();
        mMap.put(11,80);mMap.put(12,67);mMap.put(13, 67);
        mMap.put(21,67);mMap.put(22, 75);
        mFingerprint.add(mMap);

        // 1-3
        mMap = new HashMap<>();
        mMap.put(11,80);mMap.put(12,70);mMap.put(13, 64);
        mMap.put(21,67);mMap.put(22, 72);
        mFingerprint.add(mMap);

        // before 2-1
        mMap = new HashMap<>();
        mMap.put(12,80);mMap.put(13, 68);
        mMap.put(21, 62);mMap.put(22, 70);mMap.put(23, 77);
        mFingerprint.add(mMap);

        // 2-1
        mMap = new HashMap<>();
        mMap.put(13, 75);
        mMap.put(21, 58);mMap.put(22, 68);mMap.put(23, 75);
        mFingerprint.add(mMap);

        // before 2-2
        mMap = new HashMap<>();
        mMap.put(22, 60);mMap.put(21, 70);mMap.put(23, 72);
        mFingerprint.add(mMap);

        // 2-2
        mMap = new HashMap<>();
        mMap.put(22, 58);mMap.put(23, 68);mMap.put(21, 72);
        mMap.put(32, 68);
        mFingerprint.add(mMap);

        // before 2-3
        mMap = new HashMap<>();
        mMap.put(22,68);mMap.put(23, 64);
        mMap.put(32,72);mMap.put(33, 77);
        mFingerprint.add(mMap);

        // 2-3
        mMap = new HashMap<>();
        mMap.put(22,78);mMap.put(23,60);
        mMap.put(32,80);
        mFingerprint.add(mMap);
    }

    double sum, min;
    int count, minloc, major, minor,i,j;

    public int[] getLocation(Vector<Beacon> beaconList)
    {
        int[][] tempFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
        min = 999999999;
        count = minloc = 0;

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

        for(HashMap<Integer,Integer> map : mFingerprint)
        {
            sum = 0;
            for(Beacon beacon : beaconList)
            {
                major = beacon.getMajor();
                minor = beacon.getMinor();
                if((major*3+minor) % 2 == 1 && map.get(major * 10 + minor) != null)
                    sum += Math.pow(Math.pow(map.get(major * 10 + minor), 2) - Math.pow(beacon.getRSSI(), 2), 2);
            }
            if(sum < min) {
                min = sum;
                minloc = count;
            }
            count++;
        }
        Log.i("MAP","Estimated: ("+trans[minloc][0]+","+trans[minloc][1]+")");
        return trans[minloc];
    }
}
