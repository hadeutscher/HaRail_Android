LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := HaRail
LOCAL_SRC_FILES := HaRail.cpp HaRail/HaRail/Graph.cpp HaRail/HaRail/GTFSReader.cpp HaRail/HaRail/GTFSDataSource.cpp HaRail/HaRail/IDataSource.cpp HaRail/HaRail/lib_api.cpp HaRail/HaRail/StringTokenizer.cpp HaRail/HaRail/Utils.cpp
LOCAL_C_INCLUDES += D:/clib/boost
include $(BUILD_SHARED_LIBRARY)
