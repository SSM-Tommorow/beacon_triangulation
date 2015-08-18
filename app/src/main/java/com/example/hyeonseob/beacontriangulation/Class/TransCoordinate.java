package com.example.hyeonseob.beacontriangulation.Class;

import android.util.Log;

public class TransCoordinate {
    public final static int MAP_WIDTH = 840, MAP_HEIGHT = 1080, MAP_HORIZONTAL_BLOCK = 70, MAP_VERTICAL_BLOCK = 90;
    public final static double BLOCK_WIDTH = MAP_WIDTH / MAP_HORIZONTAL_BLOCK, BLOCK_HEIGHT = MAP_HEIGHT / MAP_VERTICAL_BLOCK;
    private final static double MAP_LEFT_MARGIN_RATIO = 0, MAP_TOP_MARGIN_RATIO = 0;
    private double mWRatio, mHRatio, mLeftMargin, mTopMargin, mMapWidth, mMapHeight;
    private int mBlockWidth, mBLockHeight;

    public TransCoordinate () {
        mWRatio = mHRatio = mLeftMargin = 0;
        mMapWidth = mMapHeight = mBlockWidth = mBLockHeight = 0;
    }

    public void setMapSize(int w, int h) {
        mMapWidth = w;
        mMapHeight = h;
        mWRatio = mMapWidth / MAP_WIDTH;
        mHRatio = mMapHeight / MAP_HEIGHT;
        mLeftMargin = mMapWidth * MAP_LEFT_MARGIN_RATIO;
        mTopMargin = mMapHeight * MAP_TOP_MARGIN_RATIO;
        mBlockWidth = (int)(mWRatio * BLOCK_WIDTH);
        mBLockHeight = (int)(mHRatio * BLOCK_HEIGHT);

        Log.i("MAP", "Width Ratio: " + mWRatio + ", Height Ratio: " + mHRatio + ", Left Margin: " + mLeftMargin + ", Block Size: " + mBlockWidth + "," + mBLockHeight);
    }

    public int[] getPixelPoint(int x, int y){
        int point[] = {0,0};
        point[0] = (int)(mLeftMargin + (mBlockWidth * x) + 0.5);
        point[1] = (int)(mTopMargin + (mBLockHeight * y) + 0.5);
        return point;
    }

    public int[] getCoordinate(double x, double y){
        int point[] = {0,0,0};
        point[1] = (int)(x / mWRatio / BLOCK_WIDTH);
        point[2] = (int)(y / mHRatio / BLOCK_HEIGHT);
        point[0] = point[2]*MAP_VERTICAL_BLOCK + point[1] + 1;
        return point;
    }

    public int getBlockWidth() { return mBlockWidth; }
    public int getBLockHeight() { return mBLockHeight; }
}
