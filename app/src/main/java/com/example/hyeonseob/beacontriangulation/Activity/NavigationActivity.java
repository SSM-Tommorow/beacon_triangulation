package com.example.hyeonseob.beacontriangulation.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.Class.Beacon;
import com.example.hyeonseob.beacontriangulation.Class.DBManager;
import com.example.hyeonseob.beacontriangulation.Class.KalmanFilter;
import com.example.hyeonseob.beacontriangulation.Class.TransCoordinate;
import com.example.hyeonseob.beacontriangulation.R;
import com.example.hyeonseob.beacontriangulation.RECO.RECOActivity;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class NavigationActivity extends RECOActivity implements RECORangingListener {
    public final static int WINDOW_SIZE = 20;
    private final static int INITIAL_STATE = 0, UPDATE_STATE = 1, ESTIMATION_STATE = 2;

    private int mCurrentState = INITIAL_STATE;
    private float mX, mY, mDX, mDY;
    private int mMajor, mMinor, mRSSI;
    private int mMapHeight, mMapWidth, mBlockWidth, mBlockHeight;
    private int[][] mBeaconFlag = {{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private Vector<Beacon> mBeaconList;

    private ImageView mCircleView, mMapImageView;
    private StringBuffer mStrBuff;
    private RelativeLayout mMapLayout;
    private TextView mIDTextView, mRSSITextView, mStatusTextView;
    private Drawable mRedButton, mGrayButton, mBlueButton;

    private TransCoordinate mTransCoord;
    private DBManager mDBManager;

    //위치그리기
    RelativeLayout Linear;
    private LocationView lm;

    float wid_dis;
    float hei_dis;

    //함수 내부 전역변수
    //하이패스필터
    float HPFconst = 0.01f;
    float HPF_prev = 10000;

    //무빙 에버리지 필터
    float[] MAF_Data = new float[10];
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
    float MD_Const = 0.7f; //상수 K
    double MD_Min_const = 0.005;

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
    SensorEventListener gyroL;

    Sensor accSensor; // 가속도
    Sensor magSensor; // 자기
    Sensor gyroSensor; //회전

    float[] Mag_data = new float[3]; //지자기 데이터
    float[] Kalmag_data = new float[3];
    float[] Acc_data = new float[3]; // 칼만 가속 데이터
    float[] Kalacc_data = new float[3];
    float[] Gyro_data = new float[3]; //자이로 데이터
    float[] Ori_data = new float[3]; //방향 데이터
    float[] KalOri_data = new float[3]; //칼만 방향 데이터

    private KalmanFilter[] Kalman_acc = new KalmanFilter[3];
    private KalmanFilter[] Kalman_ori = new KalmanFilter[3];
    private KalmanFilter[] Kalman_mag = new KalmanFilter[3];


    int width, height; //화면의 폭과 높이
    float mCurrentX, mCurrentY; //이미지 현재 좌표
    float dx, dy; //캐릭터가 이동할 방향과 거리
    int cw, ch; //캐릭터의 폭과 높이
    Bitmap character;//캐릭터 비트맵 이미지
    Bitmap resized;
    int Naviflag = 0;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_navigation);
        Naviflag = 0;
        lm = new LocationView(this);
        Linear = (RelativeLayout) findViewById(R.id.Linear1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        Linear.addView(lm, params);

        for(int i=0; i<10; i++){
            MAF_Data[i] = 0;
        }

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
        gyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE); // 회전

        accL = new accListener();
        magL = new magListener();
        gyroL = new gyroListener();

        mRedButton = getResources().getDrawable(R.drawable.red_button);
        mGrayButton = getResources().getDrawable(R.drawable.gray_button);
        mBlueButton = getResources().getDrawable(R.drawable.blue_button);
        mMapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        mMapImageView = (ImageView) findViewById(R.id.mapImageView);
        mIDTextView = (TextView) findViewById(R.id.idTextView);
        mRSSITextView = (TextView) findViewById(R.id.RSSITextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);

        mStrBuff = new StringBuffer();
        mCircleView = new ImageView(this);
        mCircleView.setImageDrawable(mGrayButton);
        mMapLayout.addView(mCircleView);
        mMapLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mX = event.getX();
                        mY = event.getY();
                        mDX = mX - mCircleView.getX();
                        mDY = mY - mCircleView.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCircleView.setX(event.getX() - mDX);
                        mCircleView.setY(event.getY() - mDY);
                        break;
                    case MotionEvent.ACTION_UP:
                        mX = mCircleView.getX();
                        mY = mCircleView.getY();
                        int[] point = mTransCoord.getCoordinate(mX, mY);
                        mIDTextView.setText("ID: " + point[0] + ", X: " + (point[1] + 1) + ", Y: " + (point[2] + 1));
                        break;
                }
                return true;
            }
        });

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

                mCircleView.setLayoutParams(new RelativeLayout.LayoutParams(mBlockWidth * 2, mBlockHeight * 2));
            }
        });
    }


    public void updatedata()
    {
        float Vectordata = GetEnergy(Kalacc_data[0], Kalacc_data[1], Kalacc_data[2]);
        Vectordata = GetHPFdata(Vectordata);
        Vectordata = MovingAverageFilter(Vectordata);
        Vectordata = Max_Min_check(Vectordata);
        Vectordata = Moving_Distance(Vectordata);
        Outputdata = Cal_Mapworking(Vectordata, ((float)(Math.toDegrees(KalOri_data[0]) + 360) % 360)+240);
        dx =((Outputdata[0]) /  wid_dis);
        dy = ((Outputdata[1]) /  hei_dis);
    }

    @Override
    public void onResume(){
        super.onResume();

        sm.registerListener(accL, accSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(magL, magSensor, SensorManager.SENSOR_DELAY_UI);//20ms // 자력
        sm.registerListener(gyroL, gyroSensor, SensorManager.SENSOR_DELAY_GAME);//20ms // 회전
    }


    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(accL);
        sm.unregisterListener(magL);
        sm.unregisterListener(gyroL);
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

            mCurrentX = 1250;
            mCurrentY = 700;
            dx = 0;
            dy = 0;
            character = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            resized = Bitmap.createScaledBitmap(character, 80, 80, true);
            cw = resized.getWidth();
            ch = resized.getHeight();

            mHandler.sendEmptyMessageDelayed(0, 20);
        }

        public void onDraw(Canvas canvas){
            updatedata();
            /*
            if(!mTransCoord.collisionDetection((int)mCurrentX, (int)mCurrentY, (int)(mCurrentX-dx), (int)(mCurrentY-dy)))
            {
                mCurrentX -= dx;
                mCurrentY -= dy;
            }
            */
            mCurrentX -= dx;
            mCurrentY -= dy;
            canvas.drawBitmap(resized, mCurrentX -cw, mCurrentY -ch,null);
            Naviflag++;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mCurrentX = event.getX();
                    mCurrentY = event.getY();
                    mStatusTextView.setText("Sataus: start point ("+mCurrentX+","+mCurrentY+")");
                    break;
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

    private class gyroListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            Gyro_data = event.values.clone();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    protected float GetEnergy(float x, float y, float z){
        float Energy = 0.0f;

        Energy = (float)Math.sqrt((x*x + y*y + z*z));

        return Energy;
    }

    protected float GetHPFdata(float inputdata){
        float Output = 0.0f;

        if(HPF_prev == 10000)
        {
            HPF_prev = inputdata;
            Output = 0;
        }else
        {
            Output = inputdata - HPF_prev;
            HPF_prev = (float)((inputdata * (1 - HPFconst)) + (HPF_prev * HPFconst));
        }

        return Output;
    }

    protected float MovingAverageFilter(float inputdata){

        float Output = 0.0f;

        MAF_Data[MAF_num++] = inputdata;

        if(MAF_num >= 10)
            MAF_num = 0;

        if(MAF_count < 10)
            MAF_count++;

        for(int i=0; i<10; i++)
        {
            Output += MAF_Data[i];
        }

        Output /= MAF_count;

        return Output;
    }

    protected float Max_Min_check(float inputdata){
        float Output = 0.0f;

        if(MM_input_prev == 1000){
            MM_output_value = inputdata;
            Output = 0;
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
        float Output = 0.0f;

        if(Math.abs(inputdata) > 0.02)
        //if(inputdata != 0)
        {
            if(MD_input_prev == 0){
                MD_input_prev = inputdata;
            }else{
                MD_dis_prev = (float)(MD_dis_prev + MD_Const * Math.sqrt(Math.sqrt((Math.abs(inputdata)+Math.abs(MD_input_prev)))));
                MD_input_prev = 0;
                MD_num++;
            }
        }
        Output = MD_dis_prev;

        return Output;
    }

    protected float GetLPFdata(float inputdata){
        float Output = 0.0f;

        if(LPF_input_prev == 10000){
            Output = inputdata;
        }else{
            Output = (float)(0.9355*LPF_output_prev + 0.0323*inputdata + 0.0323* LPF_input_prev);
        }

        return Output;
    }

    protected float[] Cal_Mapworking(float distance, float angle){
        float[] Output = new float[2];

        if(MW_prev_dis == distance){
            Output[0] = 0;
            Output[1] = 0;
        }else{
            Output[0] = (float)(((distance - MW_prev_dis)*100) * Math.cos((double)(angle*Math.PI / 180)));
            Output[1] = (float)(((distance - MW_prev_dis)*100) * Math.sin((double)(angle*Math.PI / 180)));
            MW_prev_dis = distance;
        }

        MW_prev_x = Output[0];
        MW_prev_y = Output[1];

        return Output;
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> collection, RECOBeaconRegion recoBeaconRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoBeaconRegion.getUniqueIdentifier() + ", number of beacons ranged: " + collection.size());

        mStrBuff.setLength(0);
        mStrBuff.append("RSSI:\n");
        for(RECOBeacon beacon : collection)
        {
            mMajor = beacon.getMajor()-1;
            mMinor = beacon.getMinor();
            mRSSI = beacon.getRssi();

            mStrBuff.append("BeaconID: (");
            mStrBuff.append(mMajor + 1);
            mStrBuff.append(",");
            mStrBuff.append(mMinor);
            mStrBuff.append("), RSSI: ");
            mStrBuff.append(mRSSI);
            mStrBuff.append("\n");

            mBeaconList.add(new Beacon(mMajor*3+mMinor,mMajor,mMinor,mRSSI));
        }
        mRSSITextView.setText(mStrBuff.toString());

        mDBManager.setTextView(mStatusTextView);
        if(mCurrentState == UPDATE_STATE)
        {
        }
        else if(mCurrentState == ESTIMATION_STATE)
        {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int[] result = null;
        if(resultCode == RESULT_OK)
        {
            result = data.getIntArrayExtra("rssi_list");

            StringBuffer sb = new StringBuffer();
            for(int a : result)
            {
                sb.append(a);
                sb.append(", ");
            }
            mRSSITextView.setText(sb.toString());

            mDBManager.setTextView(mStatusTextView);
            if(requestCode == 1)
            {
                //mDBManager.insertFingerprint(result, checkedButton+1, (int)result[0]);
            }
            else if(requestCode == 2)
            {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, Menu.NONE, "Set Starting Point");
        menu.add(0, 1, Menu.NONE, "Start Navigating");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == 0) {

        }
        else if(id == 1) {
            mStatusTextView.setText("Status: Start Navigating");
            mCurrentState = ESTIMATION_STATE;
            mRecoManager.setRangingListener(this);
            mRecoManager.bind(this);
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
