/* Copyright (C) 2015 haha01haha01

* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "HaRail/HaRail/lib_api.h"
#include <string.h>
#include <jni.h>

jstring c2jString(const string& x, JNIEnv *env) {
	return env->NewStringUTF(x.c_str());
}

string j2cString(jstring x, JNIEnv *env) {
	const char *c_str = env->GetStringUTFChars(x, JNI_FALSE);
	string result(c_str);
	env->ReleaseStringUTFChars(x, c_str);
	return result;
}

jintArray c2jIntArray(const vector<int>& x, JNIEnv *env) {
	jintArray result;
	result = env->NewIntArray(x.size());
	if (result == nullptr) {
		return nullptr; /* out of memory error thrown */
	}
	jint *buf = new jint[x.size()];
	int i = 0;
	for (int curr : x) {
		buf[i++] = curr;
	}
	env->SetIntArrayRegion(result, 0, x.size(), buf);
	delete[] buf;
	return result;
}

extern "C" jboolean Java_com_haha01haha01_harail_HaRailAPI_loadData(JNIEnv *env,
		jobject thiz, jint date, jint start_time, jstring data_root) {

	return HaRail::HaRailAPI::loadData(date, start_time,
			j2cString(data_root, env));
}

extern "C" jstring Java_com_haha01haha01_harail_HaRailAPI_getLastError(
		JNIEnv *env, jobject thiz) {
	return c2jString(HaRail::HaRailAPI::getLastError(), env);
}

extern "C" jintArray Java_com_haha01haha01_harail_HaRailAPI_getRoutes(
		JNIEnv *env, jobject thiz, jint start_time, jint start_station_id,
		jint dest_station_id) {
	return c2jIntArray(
			HaRail::HaRailAPI::getRoutes(start_time, start_station_id,
					dest_station_id), env);
}

extern "C" jstring Java_com_haha01haha01_harail_HaRailAPI_getRoutesStr(
		JNIEnv *env, jobject thiz, jint start_time, jint start_station_id,
		jint dest_station_id) {
	return c2jString(
			HaRail::HaRailAPI::getRoutesStr(start_time, start_station_id,
					dest_station_id), env);
}

extern "C" jintArray Java_com_haha01haha01_harail_HaRailAPI_getWholeTrainPath(
		JNIEnv *env, jobject thiz, jint train_id) {
	return c2jIntArray(HaRail::HaRailAPI::getWholeTrainPath(train_id), env);
}
