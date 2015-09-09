package com.example.hyeonseob.beacontriangulation.Class;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;

public class DeviceManager {
    public final int DEVICE_S4_SM = 0, DEVICE_S4_HB = 1, DEVICE_NOTE4_HS = 2, DEVICE_S4_SM_2 = 3;
    private int mDeviceNum;
    private String mAndroidId;
    private HashMap<String, Integer> mDevice;

    public DeviceManager(Context context){
        mDevice = new HashMap<>();
        mDevice.put("bb8d2a337785d4b6", DEVICE_S4_SM);
        mDevice.put("e781f0afb98d1b73", DEVICE_NOTE4_HS);
        mDevice.put("d58d19d96ff4b95b", DEVICE_S4_HB);
        mDevice.put("4e45d9be75d1aff", DEVICE_S4_SM_2);

        mAndroidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.i("DeviceManager",mAndroidId);
        if(mDevice.containsKey(mAndroidId))
            mDeviceNum = mDevice.get(mAndroidId);
        else
            mDeviceNum = -1;
    }

    public String getDeviceString() {
        switch(mDeviceNum){
            case DEVICE_S4_SM_2: return "S4_SSM_2";
            case DEVICE_NOTE4_HS: return "NOTE4_Hyeonseob";
            case DEVICE_S4_HB: return "S4_Hanbin";
            case DEVICE_S4_SM: return "S4_SSM";
            default:
                return "ERROR!";
        }
    }
    public int getDeviceNum(){ return mDeviceNum; }
}
