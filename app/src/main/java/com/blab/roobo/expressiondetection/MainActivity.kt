package com.blab.roobo.expressiondetection

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.blab.roobo.expressiondetection.base.SET_IMAGE
import com.blab.roobo.expressiondetection.nativehelper.NativeHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    
//    val imageLoader: ImageLoader by lazy { ImageLoader(this, imageView) }
    
    private val handler = MainHandler(this)
    
    private val nativeHelper = NativeHelper()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        setContentView(R.layout.activity_main)
        //初始化相机
        cameraSurfaceView.mHandler = handler
        cameraSurfaceView.activity = this
        cameraSurfaceView.imageView = imageView
        cameraSurfaceView.button = button
        cameraSurfaceView.nativeHelper = nativeHelper
        //获取图片button
        button.setOnClickListener({
//            cameraSurfaceView.takePic()
//            cameraSurfaceView.getCameraData()
        })
    
        Log.d(TAG, "onCreate: pointer: ${nativeHelper.pointer}")
        
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        cameraSurfaceView.mCamera?.release()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraSurfaceView.onPermissionResult(requestCode, grantResults)
    }
    
}

class MainHandler(activity: MainActivity) : Handler() {
    
    private val TAG = "MainHandler"
    
    private val activityRef: WeakReference<MainActivity> = WeakReference(activity)
    
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            SET_IMAGE -> {
                val activity = activityRef.get()
                val bmp = msg.obj as Bitmap
                activity?.imageView?.let {
                    it.setImageBitmap(bmp)
                    it.visibility = View.VISIBLE
//                    it.postDelayed({
//                        it.visibility = View.GONE
//                    }, 3000)
                }
            }
        }
    }
}
