package com.example.hyeonseob.beacontriangulation;

import android.util.Log;

public class TransCoordinate {
    final static double MAP_WIDTH = 828, MAP_HEIGHT = 473, MAP_HORIZONTAL_BLOCK = 14, MAP_VERTICAL_BLOCK = 8, MAP_BLOCK_SIZE = 59.14;
    private final static double MAP_LEFT_MARGIN_RATIO = 0;
    private double mWRatio, mHRatio, mLeftMargin;
    private int mMapWidth, mMapHeight, mBlockSize;

    public TransCoordinate () {
        mWRatio = mHRatio = mLeftMargin = 0;
        mMapWidth = mMapHeight = mBlockSize = 0;
    }

    public void setMapSize(int w, int h) {
        if(mMapWidth != 0)
            return;

        mMapWidth = w;
        mMapHeight = h;
        mWRatio = mMapWidth / MAP_WIDTH;
        mHRatio = mMapHeight / MAP_HEIGHT;
        mLeftMargin = mMapWidth * MAP_LEFT_MARGIN_RATIO;
        mBlockSize = (int)((mMapWidth / MAP_WIDTH) * MAP_BLOCK_SIZE);

        Log.i("MAP", "Width Ratio: " + mWRatio + ",Height Ratio: " + mHRatio + ",Left Margin: " + mLeftMargin + ",Block Size: " + mBlockSize);
    }

    public int[] getPixelPoint(int x, int y){
        int point[] = {0,0};
        point[0] = (int)(mLeftMargin + (mBlockSize * x));
        point[1] = mBlockSize * y;
        return point;
    }

    public int getBlockSize(){
        return mBlockSize;
    }
}
