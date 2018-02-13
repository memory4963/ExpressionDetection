#include <jni.h>
#include <string>
#include <sstream>
//#include <opencv2/core/core.hpp>
#include "MoveDetection.h"
#include <vector>
#include <android/log.h>

extern "C"
JNIEXPORT jstring
JNICALL
Java_com_blab_roobo_expressiondetection_nativehelper_NativeHelper_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    cv::Mat mat;
    int rows = mat.rows;
    std::string hello = "Hello from C++";
    std::stringstream stream;
    stream << hello << rows;
    return env->NewStringUTF(stream.str().c_str());
}

extern "C"
JNIEXPORT jlong
JNICALL
Java_com_blab_roobo_expressiondetection_nativehelper_NativeHelper_init() {
    return (jlong) new MoveDetection();
}

extern "C"
JNIEXPORT jbyteArray
JNICALL
Java_com_blab_roobo_expressiondetection_nativehelper_NativeHelper_readImage(
        JNIEnv *env,
        jobject /* this */,
        jlong moveDetection,
        jbyteArray jdata) {
    //转换为unsigned char
    int byteSize = (int) env->GetArrayLength(jdata);
    char *data = new char[byteSize + 1];
    env->GetByteArrayRegion(jdata, 0, byteSize, reinterpret_cast<jbyte *>(data));
    data[byteSize] = '\0';

    //转换为mat
    std::vector<uchar>::size_type size = byteSize + 1;
    std::vector<uchar> jpgBytes(data, data + size);
    Mat frame = imdecode(jpgBytes, CV_LOAD_IMAGE_COLOR);
    Mat mask(frame);

//    __android_log_print(ANDROID_LOG_DEBUG, "LOG", "frame:%p, mask:%p", &frame, &mask);
//    __android_log_print(ANDROID_LOG_DEBUG, "LOG", "bgSubstractor:%p",
//                        &(((MoveDetection *) moveDetection)->bgSubtractor));

    //进行检测
    ((MoveDetection *) moveDetection)->bgSubtractor->apply(frame, mask);



    //传回byteArray
    std::vector<unsigned char> buff;
    std::vector<int> param(2);
    param[0] = cv::IMWRITE_JPEG_QUALITY;
    param[1] = 100;//default(95) 0-100
    imencode(".jpg", mask, buff, param);

    unsigned char *cast_data = reinterpret_cast<unsigned char *>(&buff[0]);

    jbyte *jb = (jbyte *) cast_data;
    jbyteArray jarray = env->NewByteArray(byteSize);
    env->SetByteArrayRegion(jarray, 0, byteSize, jb);
    return jarray;
}
