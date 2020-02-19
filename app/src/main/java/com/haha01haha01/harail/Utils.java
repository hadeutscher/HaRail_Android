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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

public final class Utils {
	public static final String data_root = new File(
			Environment.getExternalStorageDirectory(), "irw_gtfs2")
			.getAbsolutePath()
			+ "/";

	public static boolean stationsInitialized;
	public static List<String> allStationsList;
	public static Hashtable<Integer, String> stationsById;
	public static Hashtable<String, Integer> stationsByName;

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

	private static int getHeaderIndex(String[] headers, String header) throws NoSuchFieldException
	{
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].equals(header)) {
				return i;
			}
		}
		throw new NoSuchFieldException();
	}
	
	public static void readStationList() throws FileNotFoundException, IOException, NoSuchFieldException {
		stationsInitialized = false;
		allStationsList = new ArrayList<String>();
		stationsById = new Hashtable<Integer, String>();
		stationsByName = new Hashtable<String, Integer>();
		File file = new File(data_root, "stops.txt");
		BufferedReader br;
		br = new BufferedReader(new FileReader(file));
		String line;
		String[] headers = br.readLine().split(",");
		int stat_id_idx = getHeaderIndex(headers, "stop_id");
		int stat_name_idx = getHeaderIndex(headers, "stop_name");
		while ((line = br.readLine()) != null) {
			String[] params = line.split(",");
			int stat_id = Integer.parseInt(params[stat_id_idx]);
			String stat_name = params[stat_name_idx];
			stationsByName.put(stat_name, stat_id);
			stationsById.put(stat_id, stat_name);
			allStationsList.add(stat_name);
		}
		br.close();
		stationsInitialized = true;
	}

	public static String getCurrentTimeString() {
		Calendar cal = new GregorianCalendar();
		return padWithZeroes(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)), 2)
				+ padWithZeroes(Integer.toString(cal.get(Calendar.MINUTE)), 2);
	}

	public static String getCurrentDateString() {
		Calendar cal = new GregorianCalendar();
		return padWithZeroes(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2)
				+ padWithZeroes(Integer.toString(cal.get(Calendar.MONTH) + 1), 2)
				+ Integer.toString(cal.get(Calendar.YEAR)).substring(2, 4);
	}

	public static String padWithZeroes(String x, int n) {
		while (x.length() < n) {
			x = "0" + x;
		}
		return x;
	}
	
	public static boolean shouldAskPermission() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}
}
