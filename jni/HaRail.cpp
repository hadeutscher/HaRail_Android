#include "HaRail/HaRail/lib_main.h"
#include <string.h>
#include <jni.h>

extern "C" jstring
Java_com_haha01haha01_harail_MainActivity_mainHaRail( JNIEnv* env,
                                                  jobject thiz,
												  jint date,
												  jint source_station,
												  jint time,
												  jint dest_station)
{
	string result = HaRail::lib_main(date, source_station, time, dest_station);
    return env->NewStringUTF(result.c_str());
}
