/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

public final class HaRailAPI {
	public static native boolean loadData(int date, int start_time,
			String data_root);

	public static native String getLastError();

	public static native int[] getRoutes(int start_time, int start_station_id,
			int dest_station_id);

	public static native String getRoutesStr(int start_time,
			int start_station_id, int dest_station_id);

	public static native int[] getWholeTrainPath(int train_id);

	static {
		System.loadLibrary("HaRail");
	}
}
