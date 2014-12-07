#include "HaRail/HaRail/lib_main.h"
#include <string.h>
#include <jni.h>

extern "C" jstring
Java_com_haha01haha01_harail_MainActivity_androidJniBridge( JNIEnv* env,
                                                  jobject thiz )
{
	string result = HaRail::lib_main(71214, 3500, 36000, 800);
    return env->NewStringUTF(result.c_str());
}
