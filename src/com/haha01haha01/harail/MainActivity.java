/* HaRail - Public transport fastest-route finder for Israel Railways
 * Copyright(C) 2014  haha01haha01

 * This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

 * This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package com.haha01haha01.harail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	boolean source_searching = false;
	boolean dest_searching = false;
	boolean classic_mode = false;
	int curr_source = -1;
	int curr_dest = -1;

	Lock download_mutex = new ReentrantLock();
	long download_id = 0;

	// Class methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				new DownloadCompleteCallback(),
				new IntentFilter(DatabaseDownloader.FINISHED));

		initializeComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_reset) {
			if (!isFailed()) {
				resetEnvironment();
			}
			return true;
		} else if (id == R.id.action_download) {
			downloadDb();
			return true;
		} else if (id == R.id.action_set_legacy) {
			classic_mode = !classic_mode;
			item.setChecked(classic_mode);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initializeComponents() {
		// Initialize station list and search box
		if (!Utils.stationsInitialized) {
			fail("Could not read GTFS database, try downloading it via the Menu");
			return;
		}
		setListViewItems(new ArrayList<String>());

		// source search
		MirageEditText source_search = (MirageEditText)findViewById(R.id.searchSourceStation);
		source_search.setOnFocusChangeListener(new MirageTextFocusChangeCallback());
		source_search.addTextChangedListener(new MirageTextWatcher());

		// dest search
		MirageEditText dest_search = (MirageEditText)findViewById(R.id.searchDestStation);
		dest_search.setOnFocusChangeListener(new MirageTextFocusChangeCallback());
		dest_search.addTextChangedListener(new MirageTextWatcher());

		// list view
		ListView lv = (ListView) findViewById(R.id.stationsList);
		lv.setOnItemClickListener(new SearchListItemClickedCallback());

		resetEnvironment();
	}

	// Callbacks

	class MirageTextFocusChangeCallback implements View.OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			source_searching = ((MirageEditText)findViewById(R.id.searchSourceStation)).isFocused();
			dest_searching = ((MirageEditText)findViewById(R.id.searchDestStation)).isFocused();
			onSearchBoxFocusChange(v, hasFocus);
		}
	}

	class MirageTextWatcher implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			listStationsWithSearch(s.toString());
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}

	class SearchListItemClickedCallback implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			ListView lv = (ListView) findViewById(R.id.stationsList);
			String item = (String) lv.getItemAtPosition(position);
			int station = Utils.stationsByName.get(item);
			if (source_searching) {
				curr_source = station;
				((MirageEditText)findViewById(R.id.searchSourceStation)).setMirageText(item);
				findViewById(R.id.searchDestStation).requestFocus();
			} else if (dest_searching) {
				curr_dest = station;
				((MirageEditText)findViewById(R.id.searchDestStation)).setMirageText(item);
				findViewById(R.id.timeInput).requestFocus();
			} else {
				return;
			}
			updateRoute();
		}
	}

	class DownloadCompleteCallback extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			if (intent.getAction() != DatabaseDownloader.FINISHED) {
				return;
			}
			if (!intent.getBooleanExtra(DatabaseDownloader.EXTENDED_SUCCESS,
					false)) {
				Toast.makeText(getApplicationContext(),
						"Download failed, try downloading yourself",
						Toast.LENGTH_LONG).show();
			}
			unfail();
			Utils.readStationList();
			initializeComponents();
		}
	}

	public void performSearch(View view) {
		if (curr_source == -1 || curr_dest == -1) {
			return;
		}
		int date, time;
		try {
			date = Integer.parseInt(((TextView) findViewById(R.id.dateInput))
					.getText().toString());
			String time_str = Utils.padWithZeroes(
					((TextView) findViewById(R.id.timeInput)).getText()
							.toString(), 4);
			time = Integer.parseInt(time_str.substring(0, 2)) * 3600
					+ Integer.parseInt(time_str.substring(2, 4)) * 60;
		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_LONG).show();
			return;
		}
		
		if (!HaRailAPI.loadData(date, time, Utils.data_root)) {
			Toast.makeText(getApplicationContext(), HaRailAPI.getLastError(), Toast.LENGTH_LONG).show();
			return;
		}
		
		Intent intent;
		
		if (classic_mode) {
			String result = HaRailAPI.getRoutesStr(time, curr_source, curr_dest);	
			intent = new Intent(this, DisplayRouteActivity.class);
			intent.putExtra(DisplayRouteActivity.EXTRA_DATA, result);
		} else {
			int[] result = HaRailAPI.getRoutes(time, curr_source, curr_dest);
			intent = new Intent(this, routeListActivity.class);
		    intent.putExtra(routeListActivity.EXTRA_DATA, result);
		}
	    startActivity(intent);
	}

	// UI Helper Methods

	private void fail(String error) {
		findViewById(R.id.mainContainer).setVisibility(View.GONE);
		TextView tv = (TextView) findViewById(R.id.errorView);
		tv.setText(error);
		tv.setVisibility(View.VISIBLE);
	}

	private void unfail() {
		findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);
		TextView tv = (TextView) findViewById(R.id.errorView);
		tv.setText("");
		tv.setVisibility(View.GONE);
	}

	private boolean isFailed() {
		return findViewById(R.id.mainContainer).getVisibility() == View.GONE;
	}

	private void resetEnvironment() {
		((TextView) findViewById(R.id.timeInput)).setText(Utils
				.getCurrentTimeString());
		((TextView) findViewById(R.id.dateInput)).setText(Utils
				.getCurrentDateString());

		
		((MirageEditText)findViewById(R.id.searchSourceStation)).setRealText("");
		((MirageEditText)findViewById(R.id.searchDestStation)).setRealText("");
		((MirageEditText)findViewById(R.id.searchSourceStation)).setMirageText("Source");
		((MirageEditText)findViewById(R.id.searchDestStation)).setMirageText("Dest");
		((MirageEditText)findViewById(R.id.searchSourceStation)).requestFocus();
		
		listStationsWithSearch("");

		curr_source = -1;
		curr_dest = -1;
		((Button) findViewById(R.id.mainButton)).setEnabled(false);
	}

	public void updateRoute() {
		if (curr_source != -1 && curr_dest != -1) {
			Button b = (Button) findViewById(R.id.mainButton);
			b.setEnabled(true);
		}
	}

	public void onSearchBoxFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			listStationsWithSearch(((MirageEditText)v).getRealText().toString());
		} else {
			setListViewItems(new ArrayList<String>());
		}
	}

	private void listStationsWithSearch(String data) {
		List<String> result = new ArrayList<String>();
		if (data == "") {
			setListViewItems(Utils.allStationsList);
		} else {
			for (String station : Utils.allStationsList) {
				if (station.toLowerCase(Locale.ENGLISH).contains(
						data.toLowerCase(Locale.ENGLISH))) {
					result.add(station);
				}
			}
			setListViewItems(result);
		}
	}

	private void setListViewItems(List<String> items) {
		View v = findViewById(R.id.stationsList);
		ListView lv = (ListView) v;
		ArrayAdapter<String> station_adapter = new ArrayAdapter<String>(
				MainActivity.this, R.layout.my_list_item,
				(String[]) items.toArray(new String[0]));
		lv.setAdapter(station_adapter);
	}

	public void downloadDb() {
		fail("UI disabled while downloading database");
		Intent mServiceIntent = new Intent(getApplicationContext(),
				DatabaseDownloader.class);
		startService(mServiceIntent);
	}
}
