package com.example.hyeonseob.beacontriangulation.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.hyeonseob.beacontriangulation.Class.Beacon;
import com.example.hyeonseob.beacontriangulation.Class.DBManager;
import com.example.hyeonseob.beacontriangulation.Class.DeviceManager;
import com.example.hyeonseob.beacontriangulation.Class.FileManager;
import com.example.hyeonseob.beacontriangulation.Class.KalmanFilter;
import com.example.hyeonseob.beacontriangulation.Class.LocationEstimation;
import com.example.hyeonseob.beacontriangulation.Class.TransCoordinate;
import com.example.hyeonseob.beacontriangulation.Intro.MainActivity;
import com.example.hyeonseob.beacontriangulation.R;
import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class MultipleUserActivity extends RECOActivity implements RECORangingListener {
    public final static int WINDOW_SIZE = 20;
    private final static int INITIAL_STATE = 0, BEACON_AND_SENSOR_STATE = 1, SENSOR_ONLY_STATE = 2, IN_SECTION_STATE = 3;
    private final static int[] ROTATION_REVISION = {240, 240, 240, 240};
    private final static float[] MD_CONST_REVISION = {0.655f, 0.6f, 0.7f, 0.7f};

    private int mCurrentState = INITIAL_STATE;
    private int mMajor, mMinor, mRSSI, mMapHeight, mMapWidth, mRangeSize, mRangeMargin;
    private int mCWBound, mCHBound, mBWBound, mBHBound, mRWBound, mRHBound;
    private double mBlockWidth, mBlockHeight;
    private int[][] mBeaconFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private int[][][] mFingerprint;
    private int[] mResult;
    private double[] mResult2;
    private Vector<Beacon> mBeaconList;
    private List<RECOBeacon> mBeaconCollect;

    private ImageView mMapImageView, mRangeView;
    private StringBuffer mStrBuff;
    private RelativeLayout mMapLayout;
    private RelativeLayout.LayoutParams mLayoutParams;

    private LocationEstimation mLocEst;
    private TransCoordinate mTransCoord;
    private DBManager mDBManager;
    private FileManager mFileManager;
    private DeviceManager mDeviceManager;
    private int mDeviceNum;

    //위치그리기
    private RelativeLayout Linear;
    private LocationView lm;

    float wid_dis;
    float hei_dis;

    //함수 내부 전역변수
    //하이패스필터
    float HPFconst = 0.01f;
    float HPF_prev = 10000;

    //무빙 에버리지 필터
    float[] MAF_Data;
    private int windowSize = 5;
    int MAF_count = 0;
    int MAF_num = 0;

    //Max_Min Check
    float MM_input_prev = 1000;
    float MM_output_prev = 0;
    float MM_output_value = 0;
    float MM_const = 0;

    //이동거리 함수
    float MD_input_prev = 0.0f;
    float MD_dis_prev = 0.0f;//이전 이동거리
    int MD_num = 0; //걸음수

    //로우패스필터
    float LPF_input_prev = 10000;
    float LPF_output_prev = 10000;

    //Mapworking
    float MW_prev_x = 0;
    float MW_prev_y = 0;
    float MW_prev_dis = 0;


    float[] Outputdata = new float[2];
    //변수 선언부

    SensorManager sm;
    SensorEventListener accL;
    SensorEventListener magL;

    Sensor accSensor; // 가속도
    Sensor magSensor; // 자기

    float[] Mag_data = new float[3]; //지자기 데이터
    float[] Kalmag_data = new float[3];
    float[] Acc_data = new float[3]; // 칼만 가속 데이터
    float[] Kalacc_data = new float[3];
    float[] Ori_data = new float[3]; //방향 데이터
    float[] KalOri_data = new float[3]; //칼만 방향 데이터

    private KalmanFilter[] Kalman_acc = new KalmanFilter[3];
    private KalmanFilter[] Kalman_ori = new KalmanFilter[3];
    private KalmanFilter[] Kalman_mag = new KalmanFilter[3];


    int width, height; //화면의 폭과 높이
    float mCurrentX, mCurrentY; //이미지 현재 좌표
    float mBeaconX, mBeaconY, mSensorX, mSensorY;
    float mDegree;
    float dx, dy; //캐릭터가 이동할 방향과 거리
    int mCW, mCH, mBW, mBH; //캐릭터의 폭과 높이
    Bitmap mCharacterBitmap, mCRotateBitmap;
    int Naviflag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_multiple_user);

        // Add LocationView
        Naviflag = 0;
        lm = new LocationView(this);
        Linear = (RelativeLayout) findViewById(R.id.Linear1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        Linear.addView(lm, params);
        Linear.bringChildToFront(findViewById(R.id.statusLayout));
        Linear.invalidate();

        // Initilalize
        mDegree = 0.0f;
        mCurrentX = mCurrentY = mBeaconX = mBeaconY = mSensorX = mSensorY = 0.0f;
        MAF_Data = new float[10];
        Kalman_acc[0] = new KalmanFilter(0.0f);
        Kalman_acc[1] = new KalmanFilter(0.0f);
        Kalman_acc[2] = new KalmanFilter(0.0f);
        Kalman_ori[0] = new KalmanFilter(0.0f);
        Kalman_ori[1] = new KalmanFilter(0.0f);
        Kalman_ori[2] = new KalmanFilter(0.0f);
        Kalman_mag[0] = new KalmanFilter(0.0f);
        Kalman_mag[1] = new KalmanFilter(0.0f);
        Kalman_mag[2] = new KalmanFilter(0.0f);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); // 자력

        accL = new accListener();
        magL = new magListener();

        mMapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        mMapImageView = (ImageView) findViewById(R.id.mapImageView);

        // Add range view
        mLayoutParams = new RelativeLayout.LayoutParams(10,10);
        mRangeView = new ImageView(this);
        mRangeView.setImageResource(R.drawable.image_range);
        mRangeView.setAlpha(0.5f);
        mRangeView.setLayoutParams(mLayoutParams);
        mMapLayout.addView(mRangeView);

        mStrBuff = new StringBuffer();
        mTransCoord = new TransCoordinate();
        mDBManager = new DBManager();


        mMapImageView.post(new Runnable() {
            @Override
            public void run() {
                if (mMapHeight != 0)
                    return;

                mMapHeight = mMapImageView.getMeasuredHeight();
                mMapWidth = mMapImageView.getMeasuredWidth();
                Log.w("MAP", "" + mMapHeight + "," + mMapWidth);

                mTransCoord.setMapSize(mMapWidth, mMapHeight);
                mBlockWidth = mTransCoord.getBlockWidth();
                mBlockHeight = mTransCoord.getBLockHeight();
            }
        });


        // Check android id
        mDeviceManager = new DeviceManager(getApplicationContext());
        mDeviceNum = mDeviceManager.getDeviceNum();

        // Read mFingerprint from text file
        mFileManager = new FileManager(mDeviceManager.getDeviceString());
        mFingerprint = mFileManager.readFile();


        mLocEst = new LocationEstimation(mDeviceManager.getDeviceNum());
        mLocEst.setFingerprint(mFingerprint);
    }


    public void updatedata()
    {
        float Vectordata = GetEnergy(Kalacc_data[0], Kalacc_data[1], Kalacc_data[2]);
        mDegree = ((float)(Math.toDegrees(KalOri_data[0]) +360 +ROTATION_REVISION[mDeviceNum]) % 360);
        mDegree = GetLPFdata(mDegree);

        Vectordata = GetHPFdata(Vectordata);
        Vectordata = MovingAverageFilter(Vectordata);
        Vectordata = Max_Min_check(Vectordata);
        Vectordata = Moving_Distance(Vectordata);


        Outputdata = Cal_Mapworking(Vectordata, mDegree);
        dx = ((Outputdata[0]) /  wid_dis);
        dy = ((Outputdata[1]) /  hei_dis);
    }

    @Override
    public void onResume(){
        super.onResume();
        sm.registerListener(accL, accSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(magL, magSensor, SensorManager.SENSOR_DELAY_UI);//40ms // 자력
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(accL);
        sm.unregisterListener(magL);
    }

    public class LocationView extends View {
        public LocationView(Context context){
            super(context);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            width = metrics.widthPixels;
            height = metrics.heightPixels;

            wid_dis = (float)(3054.1 / width);
            hei_dis = (float)(3724.5 / height);

            dx = dy = 0;
            mCharacterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_location);

            mCRotateBitmap = Bitmap.createScaledBitmap(mCharacterBitmap, 70, 70, false);
            mCW = mCRotateBitmap.getWidth()/2;
            mCH = mCRotateBitmap.getHeight()/2;

            mCWBound = width - mCRotateBitmap.getWidth();
            mCHBound = height - mCRotateBitmap.getHeight();

            mHandler.sendEmptyMessageDelayed(0, 20);
        }

        public void onDraw(Canvas canvas){
            // Do not move until start
            if(mCurrentState != INITIAL_STATE){
                updatedata();
                mCurrentX = Math.min(Math.max(mCurrentX-dx,0),mCWBound);
                mCurrentY = Math.min(Math.max(mCurrentY-dy,0),mCHBound);
                mSensorX = Math.min(Math.max(mSensorX-dx,0),mBWBound);
                mSensorY =  Math.min(Math.max(mSensorY-dy,0),mBHBound);

            }
            canvas.drawBitmap(mCRotateBitmap, mCurrentX-mCW, mCurrentY-mCH, null);

            mRangeView.setX(Math.min(Math.max(mCurrentX, mCW), mCWBound+mCW) - mRangeMargin);
            mRangeView.setY(Math.min(Math.max(mCurrentY, mCH), mCHBound+mCH) - mRangeMargin);
            mMapLayout.updateViewLayout(mRangeView, mLayoutParams);
            Naviflag++;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(mCurrentState == INITIAL_STATE) {
                mCurrentX = mSensorX = mBeaconX = event.getX();
                mCurrentY = mSensorY = mBeaconY = event.getY();
                if(event.getAction() == MotionEvent.ACTION_UP)
                    Toast.makeText(MultipleUserActivity.this, "출발지점을 설정했습니다.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        Handler mHandler = new Handler() {               // 타이머로 사용할 Handler
            public void handleMessage(Message msg) {
                invalidate();                              // onDraw() 다시 실행
                mHandler.sendEmptyMessageDelayed(0, 20); // 10/1000초마다 실행
            }
        }; // Handler
    }

    public void onButtonClicked(View v){
        if(v.getId() == R.id.button)
        {
            if(mCurrentState == INITIAL_STATE)
            {
                if(mCurrentX == 0)
                {
                    Toast.makeText(this,"시작 위치를 터치하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                mCurrentState = SENSOR_ONLY_STATE;
                ((Button) v).setText("STOP");
                Toast.makeText(this,"측정을 시작합니다.",Toast.LENGTH_SHORT).show();
                mRecoManager.setRangingListener(this);
                mRecoManager.bind(this);
            }
            else{
                mCurrentState = INITIAL_STATE;
                ((Button) v).setText("START");
                Toast.makeText(this,"중단하였습니다.",Toast.LENGTH_SHORT).show();
                this.stop(mRegions);
                this.unbind();
            }
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("BEACON","didRangeBeaconinRegion: "+collection.size());
        mBeaconList = new Vector<>();

        try {
            for (RECOBeacon beacon : collection) {
                mMajor = beacon.getMajor() - 1;
                mMinor = beacon.getMinor();
                mRSSI = beacon.getRssi();
                mBeaconList.add(new Beacon(mMajor * 3 + mMinor, mMajor, mMinor, mRSSI));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.i("BEACON","Collection error!");
            return;
        }

        mResult = mLocEst.getLocation(mBeaconList, (int) mDegree);
        if(mResult == null)
            return;

        mResult2 = mTransCoord.getPixelPoint(mResult[0], mResult[1]);
        mBeaconX = (float)mResult2[0];
        mBeaconY = (float)mResult2[1];

        mRangeSize = Math.min(mResult[2]/20000,500);
        mRangeMargin = mRangeSize/2;
        mLayoutParams.width = mRangeSize;
        mLayoutParams.height = mRangeSize;

        mCurrentX = mBeaconX;
        mCurrentY = mBeaconY;
    }

    private class accListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            Acc_data = event.values.clone();
            //   gravity_data = event.values.clone();

            Kalacc_data[0] = (float)Kalman_acc[0].update(Acc_data[0]);
            Kalacc_data[1] = (float)Kalman_acc[1].update(Acc_data[1]);
            Kalacc_data[2] = (float)Kalman_acc[2].update(Acc_data[2]);

            if (Kalacc_data != null && Mag_data != null) {
                float[] R = new float[16];
                SensorManager.getRotationMatrix(R, null, Kalacc_data, Kalmag_data);

                SensorManager.getOrientation(R, Ori_data);
                KalOri_data[0] = (float)Kalman_ori[0].update(Ori_data[0]);
                KalOri_data[1] = (float)Kalman_ori[1].update(Ori_data[1]);
                KalOri_data[2] = (float)Kalman_ori[2].update(Ori_data[2]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }

    private class magListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            Mag_data = event.values.clone();

            Kalmag_data[0] = (float)Kalman_mag[0].update(Mag_data[0]);
            Kalmag_data[1] = (float)Kalman_mag[1].update(Mag_data[1]);
            Kalmag_data[2] = (float)Kalman_mag[2].update(Mag_data[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }

    protected float GetEnergy(float x, float y, float z){
        return (float)Math.sqrt((x*x + y*y + z*z));
    }

    protected float GetHPFdata(float inputdata){
        float Output;

        if(HPF_prev == 10000)
        {
            Output = 0;
            HPF_prev = inputdata;

        }else
        {
            Output = inputdata - HPF_prev;
            HPF_prev = (inputdata * (1 - HPFconst)) + (HPF_prev * HPFconst);
        }

        return Output;
    }

    protected float MovingAverageFilter(float inputdata){

        float Output = 0.0f;

        MAF_Data[MAF_num++] = inputdata;

        if(MAF_num >= windowSize)
            MAF_num = 0;

        if(MAF_count < windowSize)
            MAF_count++;

        for(int i=0; i<windowSize; i++)
        {
            Output += MAF_Data[i];
        }

        Output /= MAF_count;

        return Output;
    }

    protected float Max_Min_check(float inputdata){
        float Output;

        if(MM_input_prev == 1000){
            Output = 0;
            MM_output_value = inputdata;
        }else{
            if(inputdata * MM_input_prev < 0){
                if(Math.abs(MM_output_value) > MM_const){
                    Output = MM_output_value;
                    MM_output_value = 0;
                }else{
                    Output = 0;
                    MM_output_value = 0;
                }
            }else{
                if(Math.abs(MM_output_value) < Math.abs(inputdata)){
                    Output = 0;
                    MM_output_value = inputdata;
                }else{
                    Output = 0;
                }
            }
        }
        MM_input_prev = inputdata;
        return Output;
    }

    protected float Moving_Distance(float inputdata){

        float Output;

        if(Math.abs(inputdata) > 0.02)//0.02
        //if(inputdata != 0)
        {
            if(MD_input_prev == 0){
                MD_input_prev = inputdata;
            }else{
                MD_dis_prev = (float)(MD_dis_prev + MD_CONST_REVISION[mDeviceNum] * Math.sqrt(Math.sqrt((Math.abs(inputdata)+Math.abs(MD_input_prev)))));
                MD_input_prev = 0;
                MD_num++;
            }
        }
        Output = MD_dis_prev;
        return Output;
    }

    protected float GetLPFdata(float inputdata){
        float Output;

        if(LPF_input_prev == 10000) {
            Output = inputdata;
        }
        else {
            if (Math.abs(inputdata - LPF_input_prev) < 250)
                Output = (float)(0.9355*LPF_output_prev + 0.0323*inputdata + 0.0323* LPF_input_prev);
            else
                Output = inputdata;
        }
        LPF_input_prev = inputdata;
        LPF_output_prev = Output;
        return Output;
    }

    protected float[] Cal_Mapworking(float distance, float angle){
        float[] Output = new float[2];

        if(MW_prev_dis == distance){
            Output[0] = 0;
            Output[1] = 0;
        }else{
            if(distance - MW_prev_dis > 0.35) {
                Output[0] = (float) (((distance - MW_prev_dis) * 100) * Math.cos(angle * Math.PI / 180));
                Output[1] = (float) (((distance - MW_prev_dis) * 100) * Math.sin(angle * Math.PI / 180));
            }
            MW_prev_dis = distance;
        }

        MW_prev_x = Output[0];
        MW_prev_y = Output[1];

        return Output;
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
                e.printStackTrace();
            } catch (NullPointerException e) {
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
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
