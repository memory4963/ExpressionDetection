package com.hellonurse.helloclient.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.blab.roobo.expressiondetection.BuildConfig
import com.blab.roobo.expressiondetection.R
import com.blab.roobo.expressiondetection.network.OkHttpHelper
import com.blab.roobo.expressiondetection.utils.Uri2Path
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.IOException


/**
 * Created by cfm on 2017/8/4 0004.
 */
class ImageLoader(val activity: Activity, imageView: ImageView) {
    
    private val imageView: ImageView? = imageView
    private val cancelIv: ImageView?
    private val handler: Handler
    
    private val TAG = "ImageLoader"
    
    var imageFilePath = ""
    
    init {
        this.cancelIv = null
        handler = Handler(Looper.getMainLooper())
    }
    
    
    
    fun loadImage() {
        val dialog = BottomSheetDialog(activity)
        
        val view = LayoutInflater.from(activity).inflate(R.layout.bottom_select_sheet, null, false)
        val dialogTv = view.findViewById(R.id.bottom_sheet_tv) as TextView
        dialogTv.text = "请选择加载方式"
        //相册选择
        val dialogBtn1 = view.findViewById(R.id.bottom_sheet_btn1) as Button
        dialogBtn1.text = "手机相册"
        dialogBtn1.setOnClickListener {
            
            val permissions = arrayListOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (!RequestPermissionUtils.requestPermission(activity, permissions,
                    PERMISSIONS_ALBUM, "为完成相册选择，请打开sd卡读写权限")) {
                useAlbum()
            }
            
            
            dialog.dismiss()
        }
        //拍照
        val dialogBtn2 = view.findViewById(R.id.bottom_sheet_btn2) as Button
        dialogBtn2.text = "拍照"
        dialogBtn2.setOnClickListener {
            
            val permissions = arrayListOf(android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (!RequestPermissionUtils.requestPermission(activity, permissions,
                    PERMISSIONS_CAMERA, "为完成相机拍照，请打开相机及sd卡读写权限")) {
                useCamera()
            }
            
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }
    
    fun onPermissionResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_CAMERA -> {
                //获得SD卡读写和打开相机权限
                val granted = (0..grantResults.size - 1).none {
                    grantResults[it] != PackageManager
                            .PERMISSION_GRANTED
                }
                if (granted) {
                    useCamera()
                } else {
                    Toast.makeText(activity, "打开相机失败！", Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSIONS_ALBUM -> {
                //获得SD卡读写权限
                val granted = (0..grantResults.size - 1).none {
                    grantResults[it] != PackageManager
                            .PERMISSION_GRANTED
                }
                if (granted) {
                    useAlbum()
                } else {
                    Toast.makeText(activity, "打开相册失败！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RESULT_ALBUM -> {
                //相册选中
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val originalUri = data.data
                        //获得图片绝对路径
                        val temp = Uri2Path.getPath(activity, originalUri)
                        if (temp != null) {
                            imageFilePath = temp
                            if (imageView != null) {
                                val bmp = decodeSampledBitmapFromPath(imageView.width,
                                        imageView.height)
                                imageView.setImageBitmap(bmp)
                            } else {
                                if (cancelIv != null) {
                                    cancelIv.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            RESULT_CAMERA -> {
                //相机拍摄
                if (resultCode == Activity.RESULT_OK) {
                    if (imageView != null) {
                        val bmp = decodeSampledBitmapFromPath(imageView.width, imageView
                                .height)
                        imageView.setImageBitmap(bmp)
                    } else {
                        if (cancelIv != null) {
                            cancelIv.visibility = View.VISIBLE
                        }
                    }
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onActivityResult: $resultCode")
                }
            }
        }
        uploadImage()
    }
    
    fun uploadImage() {
        OkHttpHelper.uploadImage(imageFilePath, object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                val runnable = Runnable {
                    e?.printStackTrace()
                    Toast.makeText(activity, "上传图片失败！", Toast.LENGTH_SHORT).show()
                }
                handler.post(runnable)
            }
            
            override fun onResponse(call: Call?, response: Response?) {
                val runnable = Runnable {
                    Toast.makeText(activity, "上传图片成功", Toast.LENGTH_SHORT).show()
                    try {
                        val result: String = response?.body().toString()
                        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "onResponse: $result")
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
                handler.post(runnable)
            }
        })
    }
    
    fun useCamera() {
        val folderPath = File(
                Environment.getExternalStorageDirectory().absolutePath + "/HelloNurseClient/")
        if (!folderPath.exists()) {
            folderPath.mkdir()
        }
        imageFilePath = Environment.getExternalStorageDirectory().absolutePath +
                "/roobo/Expression" + System.currentTimeMillis().toString() + ".jpg"
        
        Log.d(TAG, "onClick: " + imageFilePath)
        val temp = File(imageFilePath)
        
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID + ".fileProvider", temp)
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, contentUri)
        } else {
            val imageFileUri = Uri.fromFile(temp)
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri)
        }
        activity.startActivityForResult(intent, RESULT_CAMERA)
    }
    
    fun useAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        activity.startActivityForResult(intent, RESULT_ALBUM)
    }
    
    fun decodeSampledBitmapFromPath(reqWidth: Int, reqHeight: Int): Bitmap {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false
        
        return BitmapFactory.decodeFile(imageFilePath, options)
    }
    
    fun calculateInSampleSize(options: BitmapFactory.Options,
                              reqWidth: Int, reqHeight: Int): Int {
        // 源图片的高度和宽度
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }
    
    companion object {
        
        val PERMISSIONS_ALBUM = 23302
        val PERMISSIONS_CAMERA = 23303
        val RESULT_ALBUM = 29943
        val RESULT_CAMERA = 29944
        
    }
    
}

