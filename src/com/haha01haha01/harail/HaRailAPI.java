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
