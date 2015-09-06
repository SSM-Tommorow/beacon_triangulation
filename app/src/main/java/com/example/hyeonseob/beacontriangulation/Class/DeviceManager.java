package com.example.hyeonseob.beacontriangulation.Class;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;

public class DeviceManager {
    public final int DEVICE_S4_SM = 0, DEVICE_S4_HB = 1, DEVICE_NOTE4_HS = 2, DEVICE_NOTE2_SSM = 3;
    private int mDeviceNum;
    private String mAndroidId;
    private HashMap<String, Integer> mDevice;

    public DeviceManager(Context context){
        mDevice = new HashMap<>();
        mDevice.put("bb8d2a337785d4b6", DEVICE_S4_SM);
        mDevice.put("e781f0afb98d1b73", DEVICE_NOTE4_HS);
        mDevice.put("d58d19d96ff4b95b", DEVICE_S4_HB);
        mDevice.put("15c08843ed3fd1d7", DEVICE_NOTE2_SSM);

        mAndroidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("DeviceManager",mAndroidId);
        if(mDevice.containsKey(mAndroidId))
            mDeviceNum = mDevice.get(mAndroidId);
        else
            mDeviceNum = -1;
    }

    public String getDeviceString() {
        switch(mDeviceNum){
            case DEVICE_NOTE2_SSM: return "NOTE2_SSM";
            case DEVICE_NOTE4_HS: return "NOTE4_Hyeonseob";
            case DEVICE_S4_HB: return "S4_Hanbin";
            case DEVICE_S4_SM: return "S4_SSM";
            default:
                return "ERROR!";
        }
    }
    public int getDeviceNum(){ return mDeviceNum; }
}
