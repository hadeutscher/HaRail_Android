/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	private static final int READ_REQUEST = 100;
	private static final int WRITE_REQUEST = 200;
	private static final String SOURCE_PREF = "com.haha01haha01.harail.MainActivity.curr_source";
	private static final String DEST_PREF = "com.haha01haha01.harail.MainActivity.curr_dest";
	
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
		// Boilerplate init stuff
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Register a listener for download DB completion
		LocalBroadcastManager.getInstance(this).registerReceiver(
				new DownloadCompleteCallback(),
				new IntentFilter(DatabaseDownloader.FINISHED));

		// Read the stations list BEFORE we initialize
		if (Utils.shouldAskPermission()) {
			requestReadPermission();
		} else {
			readStationList();
		}
		
		// Init the environment
		initializeComponents();
	}
	
	@TargetApi(23)
	private void requestReadPermission() {
		int code = READ_REQUEST;
		String[] perms = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
		if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(perms, code);
		} else {
			onRequestPermissionsResult(code, perms, new int[] { PackageManager.PERMISSION_GRANTED });
		}
	}
	
	@TargetApi(23)
	private void requestWritePermission() {
		int code = WRITE_REQUEST;
		String[] perms =  new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
		if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(perms, code);
		} else {
			onRequestPermissionsResult(code, perms, new int[] { PackageManager.PERMISSION_GRANTED });
		}
	}

	 @Override
	 public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
	     if (requestCode == READ_REQUEST) {
	    	 if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	    		 readStationList();
	    	 } else {
	    		 final Activity context = this;
	    		 new AlertDialog.Builder(this)
		    		 .setTitle("Error")
		    		 .setMessage("You must allow HaRail to read from the external storage, otherwise it cannot read its DB.")
		    		 .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							context.finish();
						}
						
					})
					.create()
					.show();
	    	 }
	     } else if (requestCode == WRITE_REQUEST) {
	    	 if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
	    		 downloadDb();
	    	 } else {
	    		 new AlertDialog.Builder(this)
	    		 	.setTitle("Error")
	    		 	.setMessage("You must allow HaRail to write to the external storage in order to download the DB.")
	    		 	.create()
	    		 	.show();
	    	 }
	     }
	 }

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void checkUserNetworking()
	{
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connManager.getActiveNetworkInfo();
		if (ni == null || !ni.isConnected()) {
			new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("You are not connected to any network. Please connect and try again.")
				.create()
				.show();
		} else if (ni.getType() != ConnectivityManager.TYPE_WIFI) {
			new AlertDialog.Builder(this)
				.setTitle("Data Usage Warning")
				.setMessage("This is a large download; To avoid mobile data costs, it is recommended that you connect to a Wi-Fi network. Download anyway?")
				.setPositiveButton("Go", new DialogInterface.OnClickListener() {
	        
					@Override
					public void onClick(DialogInterface dialog, int which) {
			            dialog.dismiss();
			            requestPermissionsAndDownloadDb();
			        }
	
			    })
			
			    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
			            // Do nothing
			            dialog.dismiss();
			        }
			    })
				.create()
				.show();
		} else {
			requestPermissionsAndDownloadDb();
		}
	}
	
	private void getUserConfirmation()
	{
		new AlertDialog.Builder(this)
		.setTitle("Confirm Download")
		.setMessage("Updating the DB requires downloading over 100MB, and a few minutes of heavy CPU usage. You do not have to leave your screen on during this time.\nAre you ready?")
		.setPositiveButton("Go", new DialogInterface.OnClickListener() {
	        
			public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	            checkUserNetworking();
	        }

	    })
	
	    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            // Do nothing
	            dialog.dismiss();
	        }
	    })
	    .create()
	    .show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_reset) {
			if (!isFailed()) {
				resetEnvironment(true);
			}
			return true;
		} else if (id == R.id.action_download) {
			getUserConfirmation();
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

		resetEnvironment(false);
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
				updateSourceBox();
				updateSourcePref();
				findViewById(R.id.searchDestStation).requestFocus();
			} else if (dest_searching) {
				curr_dest = station;
				updateDestBox();
				updateDestPref();
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
			if (!intent.getAction().equals(DatabaseDownloader.FINISHED)) {
				return;
			}
			if (!intent.getBooleanExtra(DatabaseDownloader.EXTENDED_SUCCESS,
					false)) {
				Toast.makeText(getApplicationContext(),
						"Download failed, try downloading yourself",
						Toast.LENGTH_LONG).show();
			}
			unfail();
			readStationList();
			initializeComponents();
		}
	}

	private void readStationList() {
		try {
			Utils.readStationList();
		} catch (FileNotFoundException ex) {
			Toast.makeText(getApplicationContext(), "File not found: " + ex.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException ex) {
			Toast.makeText(getApplicationContext(), "IO error: " + ex.toString(), Toast.LENGTH_LONG).show();
		} catch (NoSuchFieldException ex) {
			Toast.makeText(getApplicationContext(), "No such field: " + ex.toString(), Toast.LENGTH_LONG).show();
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

		int[] result = HaRailAPI.getAllFollowingRoutes(time, curr_source, curr_dest);
		if (result[0] == 0) {
			Toast.makeText(getApplicationContext(), HaRailAPI.getLastError(), Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(this, allRoutesListActivity.class);
		intent.putExtra(allRoutesListActivity.SOURCE_STATION, curr_source)
			  .putExtra(allRoutesListActivity.DEST_STATION, curr_dest)
			  .putExtra(allRoutesListActivity.EXTRA_DATA, result);
	    startActivity(intent);
	}
	
	public void swapSourceDest(View view) {
		int temp = curr_source;
		curr_source = curr_dest;
		curr_dest = temp;
		updateSourceBox();
		updateDestBox();
		updateSourceDestPrefs();
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

	private void updateSourceBox() {
		((MirageEditText)findViewById(R.id.searchSourceStation)).setMirageText(Utils.stationsById.containsKey(curr_source) ? Utils.stationsById.get(curr_source) : "Source");
	}
	
	private void updateSourcePref() {
		getPreferences(MODE_PRIVATE).edit()
			.putInt(SOURCE_PREF, curr_source)
			.apply();
	}
	
	private void updateDestBox() {
		((MirageEditText)findViewById(R.id.searchDestStation)).setMirageText(Utils.stationsById.containsKey(curr_dest) ? Utils.stationsById.get(curr_dest) : "Dest");
	}
	
	private void updateDestPref() {
		getPreferences(MODE_PRIVATE).edit()
			.putInt(DEST_PREF, curr_dest)
			.apply();
	}
	
	private void updateSourceDestPrefs() {
		getPreferences(MODE_PRIVATE).edit()
			.putInt(SOURCE_PREF, curr_source)
			.putInt(DEST_PREF, curr_dest)
			.apply();
	}
	
	private void resetEnvironment(boolean full) {
		((TextView) findViewById(R.id.timeInput)).setText(Utils
				.getCurrentTimeString());
		((TextView) findViewById(R.id.dateInput)).setText(Utils
				.getCurrentDateString());

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		if (full) {
			curr_source = -1;
			curr_dest = -1;
			updateSourceDestPrefs();
		} else {
			curr_source = prefs.getInt(SOURCE_PREF, -1);
			curr_dest = prefs.getInt(DEST_PREF, -1);
		}
		
		MirageEditText sourceBox = ((MirageEditText)findViewById(R.id.searchSourceStation));
		MirageEditText destBox = ((MirageEditText)findViewById(R.id.searchDestStation));
		
		sourceBox.setRealText("");
		destBox.setRealText("");
		updateSourceBox();
		updateDestBox();
		
		if (sourceBox.hasFocus()) {
			onSearchBoxFocusChange(sourceBox, true);
		} else if (destBox.hasFocus()) {
			onSearchBoxFocusChange(destBox, true);
		} else {
			onSearchBoxFocusChange(null, false);
		}
		
		findViewById(R.id.mainButton).setEnabled(curr_source != -1 && curr_dest != -1);
	}

	public void updateRoute() {
		if (curr_source != -1 && curr_dest != -1) {
			findViewById(R.id.mainButton).setEnabled(true);
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
		if (data.equals("")) {
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

	public void requestPermissionsAndDownloadDb() {
		if (Utils.shouldAskPermission()) {
			requestWritePermission();
		} else {
			downloadDb();
		}
	}
	
	public void downloadDb() {
		fail("UI disabled while downloading database");
		Intent mServiceIntent = new Intent(getApplicationContext(),
				DatabaseDownloader.class);
		startService(mServiceIntent);
	}
}
