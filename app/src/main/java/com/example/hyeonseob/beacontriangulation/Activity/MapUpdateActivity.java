package com.example.hyeonseob.beacontriangulation.Activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Class.Beacon;
import com.example.hyeonseob.beacontriangulation.Class.DBManager;
import com.example.hyeonseob.beacontriangulation.Class.DeviceManager;
import com.example.hyeonseob.beacontriangulation.Class.FileManager;
import com.example.hyeonseob.beacontriangulation.Class.LocationEstimation;
import com.example.hyeonseob.beacontriangulation.Intro.MainActivity;
import com.example.hyeonseob.beacontriangulation.R;
import com.example.hyeonseob.beacontriangulation.Class.TransCoordinate;
import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class MapUpdateActivity extends RECOActivity implements RECORangingListener {

    public final static int WINDOW_SIZE = 10;
    private final static int INITIAL_STATE = 0, UPDATE_STATE = 1, ESTIMATION_STATE = 2;
    private final static int[][] LOCATION = {{5,62},{8,62},{12,62},{16,62},{20,63},{24,63},{28,63},{32,63},{37,63},{42,63},{46,63},{49,63},{53,73},{60,79},
            {43,59},{43,56},{44,52},{44,49},{44,45},{44,42},{44,38},{44,35},{44,31},{44,27},{44,24},{44,19},{44,14},{44,10},{44,5},{16,26},{16,29},{16,33}};

    private int[][][] mFingerprint;
    private int[] mRSSISum;

    private int mCurrentState = INITIAL_STATE;
    private float mX, mY, mDX, mDY;
    private float scale;
    private double mBlockWidth, mBlockHeight;
    private int mMajor, mMinor, mRSSI;
    private int mMapHeight, mMapWidth;
    private int mDirection, mLocation, mMeasureCount;
    private Vector<Beacon> mBeaconList;

    private ImageView mCircleView, mMapImageView;
    private StringBuffer mStrBuff;
    private RelativeLayout mMapLayout;
    private TextView mIDTextView, mRSSITextView, mStatusTextView;
    private Drawable mRedButton, mGrayButton, mBlueButton;
    private Button mUpButton;

    private LocationEstimation mLocEst;
    private TransCoordinate mTransCoord;
    private DBManager mDBManager;
    private View.OnClickListener mOcl;
    private FileManager mFileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_update);

        mRedButton = getResources().getDrawable(R.drawable.red_button);
        mGrayButton = getResources().getDrawable(R.drawable.gray_button);
        mBlueButton = getResources().getDrawable(R.drawable.blue_button);
        mMapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        mMapImageView = (ImageView) findViewById(R.id.mapImageView);
        mIDTextView = (TextView) findViewById(R.id.idTextView);
        mRSSITextView = (TextView) findViewById(R.id.RSSITextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        mUpButton = (Button)findViewById(R.id.upButton);

        mCircleView = new ImageView(this);
        mCircleView.setImageDrawable(mGrayButton);
        mMapLayout.addView(mCircleView);
        mStrBuff = new StringBuffer();
        mTransCoord = new TransCoordinate();
        mDBManager = new DBManager();

        scale = this.getResources().getDisplayMetrics().density;

        mDirection = mLocation = -1;
        mRSSISum = new int[15];

        // Read mFingerprint from text file
        DeviceManager mDeviceManager = new DeviceManager(getApplicationContext());
        mFileManager = new FileManager(mDeviceManager.getDeviceString());
        mFingerprint = mFileManager.readFile();

        //
        mLocEst = new LocationEstimation(mDeviceManager.getDeviceNum());


        // Set starting point
        mOcl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusTextView.setText("Click Button: ID = "+v.getId());
                mLocation = v.getId();
            }
        };
        mMapImageView.post(new Runnable() {
            @Override
            public void run() {
                Button button;
                double[] point;

                if (mMapHeight != 0)
                    return;

                mMapHeight = mMapImageView.getMeasuredHeight();
                mMapWidth = mMapImageView.getMeasuredWidth();
                Log.w("MAP", "" + mMapHeight + "," + mMapWidth);

                mTransCoord.setMapSize(mMapWidth, mMapHeight);
                mBlockWidth = mTransCoord.getBlockWidth();
                mBlockHeight = mTransCoord.getBLockHeight();

                mCircleView.setLayoutParams(new RelativeLayout.LayoutParams((int)(mBlockWidth * 2), (int)(mBlockHeight * 2)));

                for(int i=0; i<32; i++)
                {
                    button = new Button(MapUpdateActivity.this);
                    button.setId(i);
                    button.setText(Integer.toString(i));
                    button.setTextSize(9);
                    button.setAlpha(0.5f);
                    button.setPadding(0, 0, 0, 0);

                    //button.setLayoutParams(new RelativeLayout.LayoutParams((int) (25 * scale + 0.5f),(int) (25 * scale + 0.5f)));

                    button.setLayoutParams(new RelativeLayout.LayoutParams((int)(mBlockWidth*5), (int)(mBlockHeight*5)));
                    point = mTransCoord.getPixelPoint(LOCATION[i][0], LOCATION[i][1]);
                    button.setX((int)point[0]);
                    button.setY((int)point[1]);
                    button.setOnClickListener(mOcl);

                    mMapLayout.addView(button);
                }
            }
        });
    }

    public void onButtonClicked(View v){
        switch(v.getId()) {
            case R.id.upButton:
                mDirection = 0;
                mStatusTextView.setText("Status: Up Button");
                break;
            case R.id.rightButton:
                mDirection = 1;
                mStatusTextView.setText("Status: Right Button");
                break;
            case R.id.downButton:
                mDirection = 2;
                mStatusTextView.setText("Status: Down Button");
                break;
            case R.id.leftButton:
                mDirection = 3;
                mStatusTextView.setText("Status: left Button");
                break;
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoBeaconRegion.getUniqueIdentifier() + ", number of beacons ranged: " + collection.size());
        int temp[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        mBeaconList = new Vector<>();
        mStrBuff.setLength(0);
        mStrBuff.append("RSSI:\n");
        for(RECOBeacon beacon : collection)
        {
            mMajor = beacon.getMajor();
            mMinor = beacon.getMinor();
            mRSSI = beacon.getRssi();
            temp[(mMajor-1)*3+mMinor-1] = mRSSI;

            mStrBuff.append("BeaconID: (");
            mStrBuff.append(mMajor);
            mStrBuff.append(",");
            mStrBuff.append(mMinor);
            mStrBuff.append("), RSSI: ");
            mStrBuff.append(mRSSI);
            mStrBuff.append("\n");

            mBeaconList.add(new Beacon((mMajor-1)*3+mMinor,mMajor,mMinor,mRSSI));
        }
        mRSSITextView.setText(mStrBuff.toString());


        if(mCurrentState == UPDATE_STATE)
        {
            mMeasureCount++;
            for(int i=0; i<15; i++)
                mRSSISum[i] += (temp[i] == 0)? -100 : temp[i];

            if(mMeasureCount >= WINDOW_SIZE)
            {
                mCurrentState = INITIAL_STATE;
                this.stop(mRegions);
                this.unbind();

                for(int i=0; i<15; i++)
                    mFingerprint[mLocation][mDirection][i] = mRSSISum[i] / WINDOW_SIZE;
                mFileManager.writeFile(mFingerprint);

                mStatusTextView.setText("Status: Finish update");
            }
        }
        else if(mCurrentState == ESTIMATION_STATE)
        {
            int[] point = mLocEst.getLocation(mBeaconList, 1);
            mStatusTextView.setText("Estimated : ("+point[0]+","+point[1]+"), value : "+point[2]);

            double[] point2 = mTransCoord.getPixelPoint(point[0], point[1]);
            mCircleView.setX((int)point2[0]);
            mCircleView.setY((int)point2[1]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, Menu.NONE, "Update RSSI");
        menu.add(0, 1, Menu.NONE, "Start Estimation");
        menu.add(0, 2, Menu.NONE, "Stop Estimation");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == 0) {
            if(mDirection == -1 || mLocation == -1)
            {
                mStatusTextView.setText("Status: must select direction and location");
                return false;
            }

            mStatusTextView.setText("Sataus: Start updating (Direaciton:"+mDirection+", Location:"+mLocation+")");
            mCurrentState = UPDATE_STATE;
            mMeasureCount = 0;
            for(int i=0; i<15; i++)
                mRSSISum[i] = 0;
            mRecoManager.setRangingListener(this);
            mRecoManager.bind(this);
            mBeaconList = new Vector<>();
        }
        else if(id == 1) {
            mStatusTextView.setText("Status: Start estimating");
            mCurrentState = ESTIMATION_STATE;
            mLocEst.setFingerprint(mFingerprint);
            mRecoManager.setRangingListener(this);
            mRecoManager.bind(this);
        }
        else if(id == 2) {
            mStatusTextView.setText("Status: Stop estimating");
            mCurrentState = INITIAL_STATE;
            this.stop(mRegions);
            this.unbind();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnect() {
        Log.i("RECORangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(MainActivity.DISCONTINUOUS_SCAN);
        this.start(mRegions);
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {}

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {}

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
}
