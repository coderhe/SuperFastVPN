# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#Android.mk
#Android.mk必须以LOCAL_PATH变量开头
LOCAL_PATH := $(call my-dir)
#清除除了LOCAL_PATH以外的LOCAL_<name>变量，例如LOCAL_MODULE与LOCAL_SRC_FILES等
include $(CLEAR_VARS)
#设置编译后生成的模块名
LOCAL_MODULE := libcor32
#编译时加载Log库
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
#需要编译的源文件
LOCAL_SRC_FILES := fd_recv.cpp \
                   fd_send.cpp \
                   libcor32.cpp \

#编译为共享库，即后缀名为.so
include $(BUILD_SHARED_LIBRARY)