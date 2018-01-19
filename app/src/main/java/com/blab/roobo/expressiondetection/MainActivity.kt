package com.blab.roobo.expressiondetection

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellonurse.helloclient.utils.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    
    val imageLoader: ImageLoader by lazy { ImageLoader(this, imageView) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //获取图片button
        button.setOnClickListener {
            imageLoader.loadImage()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageLoader.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        imageLoader.onPermissionResult(requestCode, grantResults)
    }
}
