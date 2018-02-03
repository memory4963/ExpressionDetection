package com.blab.roobo.expressiondetection

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.blab.roobo.expressiondetection.base.SET_IMAGE
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    
//    val imageLoader: ImageLoader by lazy { ImageLoader(this, imageView) }
    
    private val handler = MainHandler(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化相机
        cameraSurfaceView.mHandler = handler
        cameraSurfaceView.activity = this
        cameraSurfaceView.imageView = imageView
        //获取图片button
        button.setOnClickListener({
            cameraSurfaceView.takePic()
//            cameraSurfaceView.getCameraData()
        })
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
                    it.postDelayed({
                        it.visibility = View.GONE
                    }, 3000)
                }
            }
        }
    }
}
