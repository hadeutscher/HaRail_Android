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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.LauncherActivity.ListItem;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	List<String> all_stations_list = new ArrayList<String>();
	Hashtable stationsById = new Hashtable();
	Hashtable stationsByName = new Hashtable();
	boolean source_searching = false;
	boolean dest_searching = false;
	int curr_source = -1;
	int curr_dest = -1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard,"irw_gtfs/stops.txt");
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException ex) 
		{
			return;
		}
	    String line;
	    try {
			br.readLine();
	    	while ((line = br.readLine()) != null) {
		        String[] params = line.split(",");
		        int stat_id = Integer.parseInt(params[0]);
		        String stat_name = params[1];
		        stationsByName.put(stat_name, stat_id);
		        stationsById.put(stat_id, stat_name);
		        all_stations_list.add(stat_name);
		    }
	    } catch(IOException ex)
	    {
	    }
	    setListViewItems(new ArrayList<String>());

	    // source search
	    SearchView search=(SearchView) findViewById(R.id.searchSourceStation);
        search.setQueryHint("Source");
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			source_searching = hasFocus;
			onSearchBoxFocusChange(v, hasFocus);
		}
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
            	listStationsWithSearch(arg0);
                return false;
            }
        });

        // dest search
	    search=(SearchView) findViewById(R.id.searchDestStation);
        search.setQueryHint("Dest");
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			dest_searching = hasFocus;
			onSearchBoxFocusChange(v, hasFocus);
		}
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
            	listStationsWithSearch(arg0);
                return false;
            }
        });

        // list view
        ListView lv = (ListView)findViewById(R.id.stationsList);
        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	    	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	        	ListView lv = (ListView)findViewById(R.id.stationsList);
	    		String item = (String)lv.getItemAtPosition(position);
	    		int station = (int)stationsByName.get(item);
	    	    if (source_searching) {
	    	    	curr_source = station;
	    	    } else if (dest_searching) {
	    	    	curr_dest = station;
	    	    } else {
	    	    	return;
	    	    }
	    	    updateRouteLabel();
	    	}
    	});
        resetEnvironment();
	}
	
	private void resetEnvironment()
	{
        // Time and date
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        ((TextView)findViewById(R.id.timeInput)).setText(padWithZeroes(Integer.toString(today.hour), 2) + padWithZeroes(Integer.toString(today.minute), 2));
        ((TextView)findViewById(R.id.dateInput)).setText(padWithZeroes(Integer.toString(today.monthDay), 2) + padWithZeroes(Integer.toString(today.month + 1), 2) + Integer.toString(today.year).substring(2, 4));
        
        ((SearchView)findViewById(R.id.searchSourceStation)).setQuery("", false);
        ((SearchView)findViewById(R.id.searchDestStation)).setQuery("", false);
        ((SearchView)findViewById(R.id.searchSourceStation)).requestFocus();
        
        curr_source = -1;
        curr_dest = -1;
        Button b = (Button)findViewById(R.id.mainButton);
		b.setEnabled(false);
		
        ((TextView)findViewById(R.id.selectedStationsView)).setText("");
        clearOutput();
	}
	
	private String padWithZeroes(String x, int n)
	{
		while (x.length() < n) {
			x = "0" + x; 
		}
		return x;
	}
	
	public void updateRouteLabel()
	{
		TextView tv = (TextView)findViewById(R.id.selectedStationsView);
		String source = curr_source == -1 ? "" : (String)stationsById.get(curr_source);
		String dest = curr_dest == -1 ? "" : (String)stationsById.get(curr_dest);
		tv.setText(source + " to " + dest);
		if (curr_source != -1 && curr_dest != -1) {
			Button b = (Button)findViewById(R.id.mainButton);
			b.setEnabled(true);
		}
		clearOutput();
	}
	
	public void onSearchBoxFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
			
		if (hasFocus) {
			listStationsWithSearch(((SearchView)v).getQuery().toString());
		} else {
			setListViewItems(new ArrayList<String>());
		}
		clearOutput();
	}
	
	private void clearOutput()
	{
		((TextView)findViewById(R.id.outputText)).setText("");
	}
	
	private void listStationsWithSearch(String data)
	{
		List<String> result = new ArrayList<String>();
		if (data == "") {
			setListViewItems(all_stations_list);
		} else {
			for (String station : all_stations_list) {
				if (station.toLowerCase(Locale.ENGLISH).contains(data.toLowerCase(Locale.ENGLISH))) {
					result.add(station);
				}
			}
			setListViewItems(result);
		}
		clearOutput();
	}
	
	private void setListViewItems(List<String> items)
	{
		ListView lv = (ListView)findViewById(R.id.stationsList);
		ArrayAdapter station_adapter = new ArrayAdapter<String>(MainActivity.this, 
	            R.layout.my_list_item,
	            (String[])items.toArray(new String[0]));
		lv.setAdapter(station_adapter);
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
			resetEnvironment();
			return true;
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
	
	public void performSearch(View view) {
		if (curr_source == -1 || curr_dest == -1) {
			return;
		}
		//findViewById(R.id.searchSourceStation).clearFocus();
		//findViewById(R.id.searchDestStation).clearFocus();
	    TextView tv = (TextView)findViewById(R.id.outputText);
	    tv.setText("Processing...");
	    int date, time;
	    try {
	    	date = Integer.parseInt(((TextView)findViewById(R.id.dateInput)).getText().toString());
	    	String time_str = padWithZeroes(((TextView)findViewById(R.id.timeInput)).getText().toString(), 4);
	    	time = Integer.parseInt(time_str.substring(0,2)) * 3600 + Integer.parseInt(time_str.substring(2, 4)) * 60;
	    } 
	    catch (NumberFormatException e)
	    {
	    	tv.setText("Error!");
	    	return;
	    }
	    String result = mainHaRail(date, curr_source, time, curr_dest);
	    tv.setText(result);
	}
	
	public native String mainHaRail(int date, int source_station, int time, int dest_station);
	static {
        System.loadLibrary("HaRail");
    }
}
