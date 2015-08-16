package com.example.hyeonseob.beacontriangulation;

import android.graphics.Point;

import java.util.Vector;

public class LocationEstimation {
    private int mInterval, mPrevX, mPrevY;
    private DBManager mDBManager;

    public LocationEstimation(DBManager dbmanager){
        mInterval = mPrevX = mPrevY = 0;
        mDBManager = dbmanager;
    }

    public Vector<Integer> getLocation(int x, int y, double[] result)
    {
        Vector<Integer> point = new Vector<>();
        mPrevX = x;
        mPrevY = y;
        mInterval = (int)result[0];


        return point;
    }
}
