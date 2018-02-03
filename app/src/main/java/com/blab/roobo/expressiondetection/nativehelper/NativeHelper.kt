package com.blab.roobo.expressiondetection.nativehelper

/**
 * Created by 10733 on 2018/2/2.
 */
class NativeHelper {
    
    external fun stringFromJNI(): String
    
    companion object {
        
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
    
}
