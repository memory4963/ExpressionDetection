#include <jni.h>
#include <string>
#include <sstream>
#include <opencv2/core/core.hpp>

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
