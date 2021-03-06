LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
include $(CLEAR_VARS)
LOCAL_MODULE := sqlite3
LOCAL_SRC_FILES := ./sqlite3.c
LOCAL_CFLAGS := -O3 -fpic -shared -std=c99 -DSQLITE_ENABLE_FTS3 -DSQLITE_ENABLE_FTS3_PARENTHESIS -DSQLITE_DISABLE_LFS
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
include $(CLEAR_VARS)
LOCAL_MODULE := sqlite3
LOCAL_SRC_FILES := ./sqlite3.c
LOCAL_CFLAGS := -O3 -fpic -shared -std=c99 -DSQLITE_ENABLE_FTS3 -DSQLITE_ENABLE_FTS3_PARENTHESIS -DSQLITE_DISABLE_LFS
include $(BUILD_SHARED_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),mips)
include $(CLEAR_VARS)
LOCAL_MODULE := sqlite3
LOCAL_SRC_FILES := ./sqlite3.c
LOCAL_CFLAGS := -O3 -fpic -shared -std=c99 -DSQLITE_ENABLE_FTS3 -DSQLITE_ENABLE_FTS3_PARENTHESIS -DSQLITE_DISABLE_LFS
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),x86)
include $(CLEAR_VARS)
LOCAL_MODULE := sqlite3
LOCAL_SRC_FILES := ./sqlite3.c
LOCAL_CFLAGS := -O3 -fpic -shared -std=c99 -DSQLITE_ENABLE_FTS3 -DSQLITE_ENABLE_FTS3_PARENTHESIS -DSQLITE_DISABLE_LFS
include $(BUILD_SHARED_LIBRARY)
endif

