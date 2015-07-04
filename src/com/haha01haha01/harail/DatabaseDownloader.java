/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.haha01haha01.harail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DatabaseDownloader extends IntentService {
	public static final String FINISHED = "com.haha01haha01.harail.DatabaseDownloader.FINISHED";
	public static final String EXTENDED_SUCCESS = "com.haha01haha01.harail.DatabaseDownloader.EXTENDED_SUCCESS";
	private static final String NAME = "HARAIL_DATABASE_DOWNLOADER";
	private static final String irw_gtfs_server = "gtfs.mot.gov.il";
	private static final int irw_gtfs_port = 21;
	private static final String irw_gtfs_filename = "irw_gtfs.zip";

	public DatabaseDownloader() {
		super(NAME);
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, NAME);

		NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentTitle("HaRail GTFS Database")
				.setContentText("Downloading file")
				.setSmallIcon(android.R.drawable.ic_popup_sync)
				.setProgress(0, 0, true);
		notifyManager.notify(1, builder.build());

		wakeLock.acquire();
		try {
			String filename = "harail_irw_gtfs_" + Utils.getCurrentDateString()
					+ ".zip";
			File zip_file = new File(new File(
					Environment.getExternalStorageDirectory(),
					Environment.DIRECTORY_DOWNLOADS), filename);
			if (downloadFile(irw_gtfs_server, irw_gtfs_port, "anonymous", "",
					irw_gtfs_filename, zip_file)) {
				sendFinished(extractDb(zip_file, notifyManager, builder));
			} else {
				sendFinished(false);
			}
			wakeLock.release();
		} catch (IOException e) {
			sendFinished(false);
			builder.setContentText("Download failed")
					.setSmallIcon(android.R.drawable.ic_dialog_alert)
					.setProgress(0, 0, false);
			notifyManager.notify(1, builder.build());
			wakeLock.release();
		}
	}

	private Boolean downloadFile(String server, int portNumber, String user,
			String password, String filename, File localFile)
			throws IOException {
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

			OutputStream outputStream = null;
			boolean success = false;
			try {
				outputStream = new BufferedOutputStream(new FileOutputStream(
						localFile));
				success = ftp.retrieveFile(filename, outputStream);
			} finally {
				if (outputStream != null) {
					outputStream.close();
				}
			}

			return success;
		} finally {
			if (ftp != null) {
				ftp.logout();
				ftp.disconnect();
			}
		}
	}

	private boolean extractDb(File zip_path, NotificationManager notifyManager,
			Notification.Builder builder) {
		builder.setContentText("Unzipping...");
		notifyManager.notify(1, builder.build());
		File irw_folder = new File(Environment.getExternalStorageDirectory(),
				"irw_gtfs");
		if (irw_folder.exists()) {
			if (irw_folder.isDirectory()) {
				for (String file : irw_folder.list()) {
					new File(irw_folder, file).delete();
				}
			} else {
				irw_folder.delete();
			}
		}
		if (!irw_folder.exists()) {
			if (!irw_folder.mkdir()) {
				builder.setContentText("Makedir failed")
						.setSmallIcon(android.R.drawable.ic_dialog_alert)
						.setProgress(0, 0, false);
				notifyManager.notify(1, builder.build());
				return false;
			}
		}
		unpackZip(irw_folder, zip_path);
		builder.setContentText("Finished").setProgress(0, 0, false)
				.setSmallIcon(android.R.drawable.ic_dialog_info);
		notifyManager.notify(1, builder.build());
		return true;
	}

	private boolean unpackZip(File output_dir, File zipname) {
		InputStream is;
		ZipInputStream zis;
		try {
			String filename;
			is = new FileInputStream(zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();

				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(output_dir, filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(new File(
						output_dir, filename));

				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}

				fout.close();
				zis.closeEntry();
			}

			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
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
