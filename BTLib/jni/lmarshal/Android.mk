LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
include $(CLEAR_VARS)
LOCAL_MODULE := marshal
LOCAL_MODULE_FILENAME := marshal
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../lua
LOCAL_SRC_FILES := ./lmarshal.c
LOCAL_SHARED_LIBRARIES := liblua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
include $(CLEAR_VARS)
LOCAL_MODULE := marshal
LOCAL_MODULE_FILENAME := marshal
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../lua
LOCAL_SRC_FILES := ./lmarshal.c
LOCAL_SHARED_LIBRARIES := liblua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared
include $(BUILD_SHARED_LIBRARY)
endif

