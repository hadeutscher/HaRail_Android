/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.io.input.BOMInputStream;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DatabaseDownloader extends IntentService {
	// Constants
	public static final String FINISHED = "com.haha01haha01.harail.DatabaseDownloader.FINISHED";
	public static final String EXTENDED_SUCCESS = "com.haha01haha01.harail.DatabaseDownloader.EXTENDED_SUCCESS";
	private static final String NAME = "HARAIL_DATABASE_DOWNLOADER";
	private static final String irw_gtfs_server = "gtfs.mot.gov.il";
	private static final int irw_gtfs_port = 21;
	private static final String irw_gtfs_filename = "israel-public-transportation.zip";
	private static final String newLine = "\r\n";
	private static final String outDir = "irw_gtfs2";
	private static final String notifyChannel = "hachannel";
	
	// Private fields
	private NotificationManager notifyManager = null;
	private Notification.Builder builder = null;
	private File zipFileLocal = new File(new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS), irw_gtfs_filename);
	private File irwFolder = new File(Environment.getExternalStorageDirectory(), outDir);
	private Pattern splitter = Pattern.compile(",");
	
	public DatabaseDownloader() {
		super(NAME);
	}
	
	private void setStatus(String message, int icon, int max, int progress, boolean intermediate) {
		if (builder != null && notifyManager != null) {
			Context context = getApplicationContext();
			builder.setContentTitle("HaRail GTFS Database")
					.setContentText(message)
					.setSmallIcon(icon)
					.setProgress(max, progress, intermediate)
					.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				builder.setChannelId(notifyChannel);
			}
			notifyManager.notify(1, builder.build());
		}
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
		notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new Notification.Builder(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					notifyChannel,
					"HaRail Database Downloader",
					NotificationManager.IMPORTANCE_DEFAULT);
			notifyManager.createNotificationChannel(channel);
		}
		wakeLock.acquire();
		try {
			setStatus("Downloading...", android.R.drawable.stat_sys_download, 0, 0, true);
			boolean success = downloadFile(irw_gtfs_server, irw_gtfs_port, "anonymous", "", irw_gtfs_filename, zipFileLocal);
			if (!success) {
				setStatus("Download Failed", android.R.drawable.ic_dialog_alert, 0, 0, false);
				sendFinished(false);
				return;
			}
			setStatus("Extracting...", android.R.drawable.stat_sys_download, 0, 0, true);
			success = extractDb();
			if (!success) {
				setStatus("Extract Failed", android.R.drawable.ic_dialog_alert, 0, 0, false);
				sendFinished(false);
				return;
			}
			setStatus("Finished", android.R.drawable.stat_sys_download_done, 0, 0, false);
			sendFinished(true);
		} finally {
			wakeLock.release();
		}
	}
	
	private boolean downloadFile(String server, int portNumber, String user,
			String password, String filename, File localFile) {
		FTPClient ftp = null;
		try {
			ftp = new FTPClient();
			ftp.setBufferSize(1024 * 1024);
			ftp.connect(server, portNumber);
			Log.d(NAME, "Connected. Reply: " + ftp.getReplyString());
			if (!ftp.login(user, password)) {
				return false;
			}
			Log.d(NAME, "Logged in");
			if (!ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
				return false;
			}
			Log.d(NAME, "Downloading");
			ftp.enterLocalPassiveMode();
			
			try (BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(localFile))) {
				return ftp.retrieveFile(filename, outputStream);
			} 
		} catch (IOException e) {
			return false;
		} finally {
			if (ftp != null) {
				try {
					ftp.logout();
					ftp.disconnect();
				} catch (IOException e) {
					// Not even gonna return false, since we already finished downloading 
				}
			}
		}
	}

	private boolean extractDb() {
		if (irwFolder.exists()) {
			if (irwFolder.isDirectory()) {
				for (String file : irwFolder.list()) {
					new File(irwFolder, file).delete();
				}
			} else {
				irwFolder.delete();
			}
		}
		if (!irwFolder.exists() && !irwFolder.mkdir()) {
			return false;
		}
		return unpackZip(irwFolder, zipFileLocal);
	}
	
	private BufferedReader getFile(ZipFile zf, String name) throws IOException {
		return new BufferedReader(new InputStreamReader(new BOMInputStream(zf.getInputStream(zf.getEntry(name))), StandardCharsets.UTF_8));
	}
	
	private String[] splitLine(String line) {
		return splitter.split(line);
	}
	
	private Map<String, Integer> parseGTFSHeaders(BufferedReader file) throws IOException{
		return parseGTFSHeaders(file, null);
	}
	
	private void writeLine(BufferedWriter file, String data) throws IOException {
		file.write(data);
		file.write(newLine);
	}
	
	private Map<String, Integer> parseGTFSHeaders(BufferedReader ifile, BufferedWriter ofile) throws IOException{
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		String line = ifile.readLine();
		if (ofile != null) {
			writeLine(ofile, line);
		}
		String[] headers = splitLine(line);
		for (int i = 0; i < headers.length; i++) {
			result.put(headers[i], i);
		}
		return result;
	}
	
	private BufferedWriter setFile(File output_dir, String name) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(output_dir, name))));
	}
	
	private String parseAgency(ZipFile zf) {
		try (BufferedReader file = getFile(zf, "agency.txt")) {
			Map<String, Integer> headers = parseGTFSHeaders(file);
			int agency_id_idx = headers.get("agency_id");
			int agency_name_idx = headers.get("agency_name");
			String line;
			String[] args;
			while ((line = file.readLine()) != null) {
				args = splitLine(line);
				if (args[agency_name_idx].equals("רכבת ישראל")) {
					return args[agency_id_idx];
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	private HashSet<String> parseRoutes(ZipFile zf, String irw_agency_id) {
		HashSet<String> irw_routes = new HashSet<String>();
		try (BufferedReader file = getFile(zf, "routes.txt")) {
			Map<String, Integer> headers = parseGTFSHeaders(file);
			int route_id_idx = headers.get("route_id");
			int agency_id_idx = headers.get("agency_id");
			String line;
			String[] args;
			while ((line = file.readLine()) != null) {
				args = splitLine(line);
				if (args[agency_id_idx].equals(irw_agency_id)) {
					irw_routes.add(args[route_id_idx]);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return irw_routes;
	}
	
	private class IRWTrips {
		HashSet<String> irw_services;
		HashSet<String> irw_trips;
	}
	
	private IRWTrips parseTrips(ZipFile zf, HashSet<String> irw_routes) {
		IRWTrips result = new IRWTrips();
		result.irw_services = new HashSet<String>();
		result.irw_trips = new HashSet<String>();
		try (BufferedReader ifile = getFile(zf, "trips.txt"); BufferedWriter ofile = setFile(irwFolder, "trips.txt")) {
			Map<String, Integer> headers = parseGTFSHeaders(ifile, ofile);
			int route_id_idx = headers.get("route_id");
			int service_id_idx = headers.get("service_id");
			int trip_id_idx = headers.get("trip_id");
			String line;
			String[] args;
			while ((line = ifile.readLine()) != null) {
				args = splitLine(line);
				if (irw_routes.contains(args[route_id_idx])) {
					result.irw_services.add(args[service_id_idx]);
					result.irw_trips.add(args[trip_id_idx]);
					writeLine(ofile, line);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}
	
	private boolean parseCalendar(ZipFile zf, HashSet<String> irw_services) {
		try (BufferedReader ifile = getFile(zf, "calendar.txt"); BufferedWriter ofile = setFile(irwFolder, "calendar.txt")) {
			Map<String, Integer> headers = parseGTFSHeaders(ifile, ofile);
			int service_id_idx = headers.get("service_id");
			String line;
			String[] args;
			while ((line = ifile.readLine()) != null) {
				args = splitLine(line);
				if (irw_services.contains(args[service_id_idx])) {
					writeLine(ofile, line);
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}
	
	private HashSet<String> parseStopTimes(ZipFile zf, HashSet<String> irw_trips) {
		HashSet<String> irw_stops = new HashSet<String>();
		try (BufferedReader ifile = getFile(zf, "stop_times.txt"); BufferedWriter ofile = setFile(irwFolder, "stop_times.txt")) {
			Map<String, Integer> headers = parseGTFSHeaders(ifile, ofile);
			int trip_id_idx = headers.get("trip_id");
			int stop_id_idx = headers.get("stop_id");
			String line;
			String[] args;
			int i = 0;
			while ((line = ifile.readLine()) != null) {
				args = splitLine(line);
				if (irw_trips.contains(args[trip_id_idx])) {
					irw_stops.add(args[stop_id_idx]);
					writeLine(ofile, line);
				}
				if (++i % 100000 == 0) {
					Log.d(NAME, Integer.toString(i));
				}
			}
		} catch (Exception e) {
			return null;
		}
		return irw_stops;
	}

	private boolean parseStops(ZipFile zf, HashSet<String> irw_stops) {
		try (BufferedReader ifile = getFile(zf, "stops.txt"); BufferedWriter ofile = setFile(irwFolder, "stops.txt")) {
			Map<String, Integer> headers = parseGTFSHeaders(ifile, ofile);
			int stop_id_idx = headers.get("stop_id");
			String line;
			String[] args;
			while ((line = ifile.readLine()) != null) {
				args = splitLine(line);
				if (irw_stops.contains(args[stop_id_idx])) {
					writeLine(ofile, line);
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean unpackZip(File output_dir, File zipname) {
		try (ZipFile zf = new ZipFile(zipname)) {
			// agency.txt
			setStatus("Extracting agency.txt...", android.R.drawable.stat_sys_download, 6, 0, false);
			String irw_agency_id = parseAgency(zf);
			if (irw_agency_id == null) {
				return false;
			}
			
			// routes.txt
			setStatus("Extracting routes.txt...", android.R.drawable.stat_sys_download, 6, 1, false);
			HashSet<String> irw_routes = parseRoutes(zf, irw_agency_id);
			if (irw_routes == null) {
				return false;
			}
			
			// trips.txt
			setStatus("Extracting trips.txt...", android.R.drawable.stat_sys_download, 6, 2, false);
			IRWTrips result = parseTrips(zf, irw_routes);
			if (result == null) {
				return false;
			}
			HashSet<String> irw_services = result.irw_services;
			HashSet<String> irw_trips = result.irw_trips;
			
			// calendar.txt
			setStatus("Extracting calendar.txt...", android.R.drawable.stat_sys_download, 6, 3, false);
			if (!parseCalendar(zf, irw_services)) {
				return false;
			}
			
			// stop_times.txt
			setStatus("Extracting stop_times.txt...", android.R.drawable.stat_sys_download, 6, 4, false);
			HashSet<String> irw_stops = parseStopTimes(zf, irw_trips);
			if (irw_stops == null) {
				return false;
			}
			
			// stops.txt
			setStatus("Extracting stops.txt...", android.R.drawable.stat_sys_download, 6, 5, false);
			if (!parseStops(zf, irw_stops)) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private void sendFinished(boolean success) {
		Intent intent = new Intent(FINISHED)
				.putExtra(EXTENDED_SUCCESS, success);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
