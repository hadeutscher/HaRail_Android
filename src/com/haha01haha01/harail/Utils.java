/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.os.Environment;
import android.text.format.Time;

public final class Utils {
	public static final String data_root = new File(
			Environment.getExternalStorageDirectory(), "irw_gtfs")
			.getAbsolutePath()
			+ "/";

	public static boolean stationsInitialized;
	public static List<String> allStationsList;
	public static Hashtable<Integer, String> stationsById;
	public static Hashtable<String, Integer> stationsByName;

	static {
		readStationList();
	}
	
	public static String makeTime(int time) {
		String hours = padWithZeroes(Integer.toString(time / 3600), 2);
		time %= 3600;
		String mins = padWithZeroes(Integer.toString(time / 60), 2);
		int dw_secs = time % 60;
		if (dw_secs == 0) {
			return hours + ":" + mins;
		}
		else {
			String secs = padWithZeroes(Integer.toString(dw_secs), 2);
			return hours + ":" + mins + ":" + secs;
		}
	}

	public static void readStationList() {
		stationsInitialized = false;
		allStationsList = new ArrayList<String>();
		stationsById = new Hashtable<Integer, String>();
		stationsByName = new Hashtable<String, Integer>();
		try {
			File sdcard = Environment.getExternalStorageDirectory();
			File file = new File(sdcard, "irw_gtfs/stops.txt");
			BufferedReader br;
			br = new BufferedReader(new FileReader(file));
			String line;
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] params = line.split(",");
				int stat_id = Integer.parseInt(params[0]);
				String stat_name = params[1];
				stationsByName.put(stat_name, stat_id);
				stationsById.put(stat_id, stat_name);
				allStationsList.add(stat_name);
			}
			br.close();
			stationsInitialized = true;
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
	}

	public static String getCurrentTimeString() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		return padWithZeroes(Integer.toString(today.hour), 2)
				+ padWithZeroes(Integer.toString(today.minute), 2);
	}

	public static String getCurrentDateString() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		return padWithZeroes(Integer.toString(today.monthDay), 2)
				+ padWithZeroes(Integer.toString(today.month + 1), 2)
				+ Integer.toString(today.year).substring(2, 4);
	}

	public static String padWithZeroes(String x, int n) {
		while (x.length() < n) {
			x = "0" + x;
		}
		return x;
	}
}
