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
import java.util.Collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hyeonseob.beacontriangulation.R;
import com.perples.recosdk.RECOBeacon;

public class RECORangingListAdapter extends BaseAdapter {
	private ArrayList<RECOBeacon> mRangedBeacons;
	private LayoutInflater mLayoutInflater;
	
	public RECORangingListAdapter(Context context) {
		super();
		mRangedBeacons = new ArrayList<RECOBeacon>();
		mLayoutInflater = LayoutInflater.from(context);
	}
	
	public void updateBeacon(RECOBeacon beacon) {
		synchronized (mRangedBeacons) {
			if(mRangedBeacons.contains(beacon)) {
				mRangedBeacons.remove(beacon);
			}
			mRangedBeacons.add(beacon);
		}
	}
	
	public void updateAllBeacons(Collection<RECOBeacon> beacons) {
		synchronized (beacons) {
			mRangedBeacons = new ArrayList<RECOBeacon>(beacons);
		}
	}
	
	public void clear() {
		mRangedBeacons.clear();
	}
	
	@Override
	public int getCount() {
		return mRangedBeacons.size();
	}

	@Override
	public Object getItem(int position) {
		return mRangedBeacons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		
		if(convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_ranging_beacon, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.recoMajor = (TextView)convertView.findViewById(R.id.recoMajor);
			viewHolder.recoMinor = (TextView)convertView.findViewById(R.id.recoMinor);
			viewHolder.recoRssi = (TextView)convertView.findViewById(R.id.recoRssi);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		RECOBeacon recoBeacon = mRangedBeacons.get(position);

		viewHolder.recoMajor.setText(recoBeacon.getMajor() + "");
		viewHolder.recoMinor.setText(recoBeacon.getMinor() + "");
		viewHolder.recoRssi.setText(recoBeacon.getRssi() + "");
		
		return convertView;
	}
	
	static class ViewHolder {
		TextView recoMajor;
		TextView recoMinor;
		TextView recoRssi;
	}

}
