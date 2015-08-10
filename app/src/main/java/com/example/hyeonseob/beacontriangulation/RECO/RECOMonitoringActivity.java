/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014-2015 Perples, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.example.hyeonseob.beacontriangulation.RECO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;

import com.example.hyeonseob.beacontriangulation.R;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;

/**
 * RECOMonitoringActivity class is to monitor regions in the foreground.
 */
public class RECOMonitoringActivity extends RECOActivity implements RECOMonitoringListener {
	
	private RECOMonitoringListAdapter mMonitoringListAdapter;
	private ListView mRegionListView;
	
	/**
	 * We recommend 1 second for scanning, 10 seconds interval between scanning, and 60 seconds for region expiration time.
	 */
	private long mScanPeriod = 1*1000L;
	private long mSleepPeriod = 10*1000L;
	
	private boolean mInitialSetting = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_monitoring);
		
		//mRecoManager will be created here. (Refer to the RECOActivity.onCreate())
		//mRecoManager 인스턴스는 여기서 생성됩니다. RECOActivity.onCreate() 메소들르 참고하세요.
		
		//Set RECOMonitoringListener (Required)
		//RECOMonitoringListener 를 설정합니다. (필수)
		mRecoManager.setMonitoringListener(this);
		
		//Set scan period and sleep period. 
		//The default is 1 second for the scan period and 10 seconds for the sleep period.
		mRecoManager.setScanPeriod(mScanPeriod);
		mRecoManager.setSleepPeriod(mSleepPeriod);
		
		/**
		 * Bind RECOBeaconManager with RECOServiceConnectListener, which is implemented in RECOActivity
		 * You SHOULD call this method to use monitoring/ranging methods successfully.
		 * After binding, onServiceConenct() callback method is called. 
		 * So, please start monitoring/ranging AFTER the CALLBACK is called.
		 */
		mRecoManager.bind(this);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		mMonitoringListAdapter = new RECOMonitoringListAdapter(this);
		mRegionListView = (ListView)findViewById(R.id.list_monitoring);
		mRegionListView.setAdapter(mMonitoringListAdapter);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();	
		this.stop(mRegions);
		this.unbind();
	}
	
	@Override
	public void onServiceConnect() {
		Log.i("RECOMonitoringActivity", "onServiceConnect");
		this.start(mRegions);
		//Write the code when RECOBeaconManager is bound to RECOBeaconService
	}

	@Override
	public void didDetermineStateForRegion(RECOBeaconRegionState recoRegionState, RECOBeaconRegion recoRegion) {
		Log.i("RECOMonitoringActivity", "didDetermineStateForRegion()");
		Log.i("RECOMonitoringActivity", "region: " + recoRegion.getUniqueIdentifier() + ", state: " + recoRegionState.toString());
		
		if(mInitialSetting) {
			mMonitoringListAdapter.updateRegion(recoRegion, recoRegionState, 0, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
			mMonitoringListAdapter.notifyDataSetChanged();
		}
		
		mInitialSetting = false;
		//Write the code when the state of the monitored region is changed
	}

	@Override
	public void didEnterRegion(RECOBeaconRegion recoRegion, Collection<RECOBeacon> beacons) {
		/**
		 * For the first run, this callback method will not be called. 
		 * Please check the state of the region using didDetermineStateForRegion() callback method.
		 */
		
		//Get the region and found beacon list in the entered region
		Log.i("RECOMonitoringActivity", "didEnterRegion() region:" + recoRegion.getUniqueIdentifier());

		mMonitoringListAdapter.updateRegion(recoRegion, RECOBeaconRegionState.RECOBeaconRegionInside, beacons.size(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
		mMonitoringListAdapter.notifyDataSetChanged();
		//Write the code when the device is enter the region
	}

	@Override
	public void didExitRegion(RECOBeaconRegion recoRegion) {
		/**
		 * For the first run, this callback method will not be called. 
		 * Please check the state of the region using didDetermineStateForRegion() callback method.
		 */
		
		Log.i("RECOMonitoringActivity", "didExitRegion() region:" + recoRegion.getUniqueIdentifier());

		mMonitoringListAdapter.updateRegion(recoRegion, RECOBeaconRegionState.RECOBeaconRegionOutside, 0, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(new Date()));
		mMonitoringListAdapter.notifyDataSetChanged();
		//Write the code when the device is exit the region
	}

	@Override
	public void didStartMonitoringForRegion(RECOBeaconRegion recoRegion) {
		Log.i("RECOMonitoringActivity", "didStartMonitoringForRegion: " + recoRegion.getUniqueIdentifier());
		//Write the code when starting monitoring the region is started successfully
	}

	@Override
	protected void start(ArrayList<RECOBeaconRegion> regions) {
		Log.i("RECOMonitoringActivity", "start");

		for(RECOBeaconRegion region : regions) {
			try {
				region.setRegionExpirationTimeMillis(60*1000L);
				mRecoManager.startMonitoringForRegion(region);
			} catch (RemoteException e) {
				Log.i("RECOMonitoringActivity", "Remote Exception");
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.i("RECOMonitoringActivity", "Null Pointer Exception");
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void stop(ArrayList<RECOBeaconRegion> regions) {
		for(RECOBeaconRegion region : regions) {
			try {
				mRecoManager.stopMonitoringForRegion(region);
			} catch (RemoteException e) {
				Log.i("RECOMonitoringActivity", "Remote Exception");
				e.printStackTrace();
			} catch (NullPointerException e) {
				Log.i("RECOMonitoringActivity", "Null Pointer Exception");
				e.printStackTrace();
			}
		}
	}
	
	private void unbind() {
		try {
			mRecoManager.unbind();
		} catch (RemoteException e) {
			Log.i("RECOMonitoringActivity", "Remote Exception");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onServiceFail(RECOErrorCode errorCode) {
		//Write the code when the RECOBeaconService is failed.
		//See the RECOErrorCode in the documents.
		return;
	}
	
	@Override
	public void monitoringDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
		//Write the code when the RECOBeaconService is failed to monitor the region.
		//See the RECOErrorCode in the documents.
		return;
	}

}
