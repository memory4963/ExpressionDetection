package com.blab.roobo.expressiondetection.nativehelper

/**
 * Created by 10733 on 2018/2/2.
 */
class NativeHelper {
    
//    external fun stringFromJNI(): String
    
    val pointer: Long
    
    init {
        pointer = init()
    }
    
    external fun init(): Long
    
    external fun readImage(pointer: Long, data: ByteArray): ByteArray
    
    external fun readCounter(pointer: Long): Boolean
    
    companion object {
        
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
    
}
