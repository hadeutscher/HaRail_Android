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

import android.os.Environment;
import android.text.format.Time;

public final class Utils {
	public static final String data_root = new File(Environment.getExternalStorageDirectory(), "irw_gtfs").getAbsolutePath() + "/";
	
	public static void readStationList(MainActivity context) throws FileNotFoundException
	{
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard,"irw_gtfs/stops.txt");
		BufferedReader br;
		br = new BufferedReader(new FileReader(file));
	    String line;
	    try {
			br.readLine();
	    	while ((line = br.readLine()) != null) {
		        String[] params = line.split(",");
		        int stat_id = Integer.parseInt(params[0]);
		        String stat_name = params[1];
		        context.stationsByName.put(stat_name, stat_id);
		        context.stationsById.put(stat_id, stat_name);
		        context.all_stations_list.add(stat_name);
		    }
	    } catch(IOException ex)
	    {
	    }
	}
	
	public static String getCurrentTimeString() {
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        return padWithZeroes(Integer.toString(today.hour), 2) + padWithZeroes(Integer.toString(today.minute), 2);
	}
	
	public static String getCurrentDateString() {
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        return padWithZeroes(Integer.toString(today.monthDay), 2) + padWithZeroes(Integer.toString(today.month + 1), 2) + Integer.toString(today.year).substring(2, 4);
	}
	
	public static String padWithZeroes(String x, int n)
	{
		while (x.length() < n) {
			x = "0" + x; 
		}
		return x;
	}
}
