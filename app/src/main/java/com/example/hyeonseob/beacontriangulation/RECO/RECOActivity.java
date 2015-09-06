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

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;

import com.example.hyeonseob.beacontriangulation.Intro.MainActivity;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOServiceConnectListener;

public abstract class RECOActivity extends Activity implements RECOServiceConnectListener {
	protected RECOBeaconManager mRecoManager;
	protected ArrayList<RECOBeaconRegion> mRegions;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), MainActivity.SCAN_RECO_ONLY, MainActivity.ENABLE_BACKGROUND_RANGING_TIMEOUT);
		//mRecoManager.setScanPeriod(100);
		mRegions = this.generateBeaconRegion();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private ArrayList<RECOBeaconRegion> generateBeaconRegion() {
		ArrayList<RECOBeaconRegion> regions = new ArrayList<RECOBeaconRegion>();
		
		RECOBeaconRegion recoRegion;
		recoRegion = new RECOBeaconRegion(MainActivity.RECO_UUID, "RECO Region");
		regions.add(recoRegion);

		return regions;
	}
	
	protected abstract void start(ArrayList<RECOBeaconRegion> regions);
	protected abstract void stop(ArrayList<RECOBeaconRegion> regions);
}
