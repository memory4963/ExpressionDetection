
cmake_minimum_required(VERSION 3.4.1)

set(opencv_DIR ${CMAKE_SOURCE_DIR}/../opencv)

add_library(lib_opencv_java3 SHARED IMPORTED)
include_directories(lib_opencv_java3 ${opencv_DIR}/include)
set_target_properties(lib_opencv_java3 PROPERTIES IMPORTED_LOCATION
                        ${opencv_DIR}/libs/${ANDROID_ABI}/libopencv_java3.so)

add_library( native-lib SHARED
             src/main/cpp/native-lib.cpp
              src/main/cpp/MoveDetection.cpp)

find_library(log-lib log )

target_link_libraries( native-lib
                       ${log-lib}
                        android
                        lib_opencv_java3)
