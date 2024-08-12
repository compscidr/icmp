# this is only for non-android operation (ie, jvm)
cmake_minimum_required(VERSION 3.16.3)
project("icmp-lib")
find_package(JNI)

include_directories(headers)
include_directories(${JNI_INCLUDE_DIRS}/linux)
add_library(icmp-lib SHARED cpp/jni_filedesc.cpp)