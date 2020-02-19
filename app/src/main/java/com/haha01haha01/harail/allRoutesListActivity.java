/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public class allRoutesListActivity extends Activity implements
		allRoutesListFragment.Callbacks {

	public static final String SOURCE_STATION = "com.haha01haha01.harail.SOURCE_STATION";
	public static final String DEST_STATION = "com.haha01haha01.harail.DEST_STATION";
	public static final String EXTRA_DATA = "com.haha01haha01.harail.EXTRA_DATA";

	private int mSourceStationId;
	private int mDestStationId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSourceStationId = getIntent().getIntExtra(SOURCE_STATION, -1);
		mDestStationId = getIntent().getIntExtra(DEST_STATION, -1);

		setContentView(R.layout.activity_all_routes_list);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Callback method from {@link allRoutesListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(int id) {
		int[] result = HaRailAPI.getRoutes(id, mSourceStationId, mDestStationId);
		if (result[0] == 0) {
			Toast.makeText(getApplicationContext(), HaRailAPI.getLastError(), Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(this, routeListActivity.class);
		intent.putExtra(routeListActivity.EXTRA_DATA, result);
		startActivity(intent);
	}
}
