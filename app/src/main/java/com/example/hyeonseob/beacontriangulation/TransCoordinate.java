package com.example.hyeonseob.beacontriangulation;

import android.util.Log;

public class TransCoordinate {
    final static double MAP_WIDTH = 828, MAP_HEIGHT = 473, MAP_HORIZONTAL_BLOCK = 14, MAP_VERTICAL_BLOCK = 8, MAP_BLOCK_SIZE = 59.14;
    private final static double MAP_LEFT_MARGIN_RATIO = 0, MAP_TOP_MARGIN_RATIO = 0;
    private double mWRatio, mHRatio, mLeftMargin, mTopMargin, mMapWidth, mMapHeight;
    private int mBlockSize;

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
        mTopMargin = mMapHeight * MAP_TOP_MARGIN_RATIO;
        mBlockSize = (int)((mMapWidth / MAP_WIDTH) * MAP_BLOCK_SIZE + 0.5);

        Log.i("MAP", "Width Ratio: " + mWRatio + ",Height Ratio: " + mHRatio + ",Left Margin: " + mLeftMargin + ",Block Size: " + mBlockSize);
    }

    public int[] getPixelPoint(int x, int y){
        int point[] = {0,0};
        point[0] = (int)(mLeftMargin + (mBlockSize * x) + 0.5);
        point[1] = (int)(mTopMargin + (mBlockSize * y) + 0.5);
        return point;
    }

    public int getBlockSize(){
        return mBlockSize;
    }
}
