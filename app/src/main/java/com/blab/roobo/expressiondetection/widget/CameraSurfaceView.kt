package com.blab.roobo.expressiondetection.widget

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.blab.roobo.expressiondetection.nativehelper.NativeHelper
import com.hellonurse.helloclient.utils.ImageLoader
import com.hellonurse.helloclient.utils.RequestPermissionUtils
import java.io.ByteArrayOutputStream
import java.io.IOException


/**
 * Created by memory4963 on 2018/1/18.
 */
class CameraSurfaceView : SurfaceView, SurfaceHolder.Callback, Camera.AutoFocusCallback {
    
    public lateinit var mHandler: Handler
    public lateinit var activity: Activity
    public lateinit var imageView: ImageView
    public lateinit var nativeHelper: NativeHelper
    
    private val mContext: Context
    private var mHolder: SurfaceHolder? = null
    var mCamera: Camera? = null
    
    private var mScreenWidth: Int = 0
    private var mScreenHeight: Int = 0
    
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        mContext = context
        getScreenMetrix(context)
        initView()
    }
    
    private fun getScreenMetrix(context: Context) {
        val WM = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        WM.defaultDisplay.getMetrics(outMetrics)
        mScreenWidth = outMetrics.widthPixels
        mScreenHeight = outMetrics.heightPixels
    }
    
    private fun initView() {
        mHolder = holder//获得surfaceHolder引用
        mHolder?.addCallback(this)
        mHolder?.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)//设置类型
    }
    
    override fun surfaceCreated(p0: SurfaceHolder?) {
        Log.d(TAG, "surfaceCreated")
        val permissions = arrayListOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!RequestPermissionUtils.requestPermission(activity, permissions,
                ImageLoader.PERMISSIONS_CAMERA, "为完成相机拍照，请打开相机及sd卡读写权限")) {
            startCamera()
        }
    }
    
    private fun startCamera() {
        if (mCamera != null) {
            mCamera?.release()
        }
        mCamera = if (Camera.getNumberOfCameras() > 1) {
            Camera.open(1)//开启相机
        } else {
            Camera.open()//开启相机
        }
        try {
            
            mCamera?.setPreviewDisplay(holder)//摄像头画面显示在Surface上
        } catch (e: IOException) {
            e.printStackTrace()
        }
        setCameraParams()
    }
    
    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        Log.d(TAG, "surfaceChanged")
        mCamera?.startPreview()
    }
    
    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        Log.d(TAG, "surfaceDestroyed")
        mCamera?.stopPreview()//停止预览
//        mCamera?.release()//释放相机资源
//        mCamera = null
        mHolder = null
    }
    
    override fun onAutoFocus(p0: Boolean, p1: Camera?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    private fun setCameraParams() {
        Log.i(TAG, "setCameraParams  width=$mScreenWidth  height=$mScreenHeight")
        mCamera?.let {
            val parameters = it.parameters
            // 获取摄像头支持的PictureSize列表
            val pictureSizeList = parameters.supportedPictureSizes
            for (size in pictureSizeList) {
                Log.i(TAG, "pictureSizeList size.width=${size.width} size.height=${size.height}")
            }
            /**从列表中选取合适的分辨率 */
            var picSize = getProperSize(pictureSizeList, mScreenHeight.toFloat() / mScreenWidth)
            if (null == picSize) {
                Log.i(TAG, "null == picSize")
                picSize = parameters.pictureSize
            }
            Log.i(TAG, "picSize.width=" + picSize!!.width + "  picSize.height=" + picSize.height)
            // 根据选出的PictureSize重新设置SurfaceView大小
            val w = picSize.width.toFloat()
            val h = picSize.height.toFloat()
            parameters.setPictureSize(picSize.width, picSize.height)
            this.layoutParams = ConstraintLayout.LayoutParams((mScreenHeight * (h / w)).toInt(), mScreenHeight)
            
            // 获取摄像头支持的PreviewSize列表
            val previewSizeList = parameters.supportedPreviewSizes
            
            for (size in previewSizeList) {
                Log.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height)
            }
            val preSize = getProperSize(previewSizeList, mScreenHeight.toFloat() / mScreenWidth)
            if (null != preSize) {
                Log.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height)
                parameters.setPreviewSize(preSize.width, preSize.height)
            }
            
            parameters.jpegQuality = 80 // 设置照片质量
            if (parameters.supportedFocusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.focusMode = android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE// 连续对焦模式
            }
            
            parameters.setPreviewFpsRange(25, 30) // 每秒预览帧数
            parameters.previewFrameRate = 3 // 每秒获取帧数
            //处理获取帧数
            it.setPreviewCallback({ data, camera ->
                val previewSize = camera.parameters.previewSize
                val yuvImage = YuvImage(data,
                        ImageFormat.NV21,
                        previewSize.width,
                        previewSize.height,
                        null)
                val stream = ByteArrayOutputStream()
//                val rgb = decodeYUV420SP(data, previewSize.width, previewSize.height)
//                val bmp = Bitmap.createBitmap(rgb, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888)
                yuvImage.compressToJpeg(Rect(0, 0, previewSize.width, previewSize.height),
                        80,
                        stream)
                val rawImage = stream.toByteArray()
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.RGB_565
                
                val readImage = nativeHelper.readImage(nativeHelper.pointer, rawImage)
                val bmp = BitmapFactory.decodeByteArray(readImage, 0, rawImage.size, options)
                imageView.setImageBitmap(bmp)
//                val temp = bitmap
//                bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.size, options)
//                imageView.setImageBitmap(bitmap)
//                temp.recycle()
//                if (!bool) {
//                    bool = true
//                    Log.d(TAG, "setCameraParams: 进入")
//                    val readImage = nativeHelper.readImage(nativeHelper.pointer, rawImage)
//                    val bmp = BitmapFactory.decodeByteArray(readImage, 0, rawImage.size, options)
//                    Log.d(TAG, "setCameraParams: bmp returned")
//                    imageView.setImageBitmap(bmp)
//                }
            })
            
            it.cancelAutoFocus()//自动对焦。
            // 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
            // TODO 这里直接设置90°不严谨，具体见https://developer.android.com/reference/android/hardware/Camera.html#setPreviewDisplay%28android.view.SurfaceHolder%29
            it.setDisplayOrientation(90)
//            it.parameters = parameters
        }
        
    }
    
    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     *
     * tip：这里的w对应屏幕的height
     * h对应屏幕的width
     *
     *
     */
    private fun getProperSize(pictureSizeList: List<Camera.Size>, screenRatio: Float): Camera.Size? {
        Log.i(TAG, "screenRatio=" + screenRatio)
        var result: Camera.Size? = null
        for (size in pictureSizeList) {
            val currentRatio = size.width.toFloat() / size.height
            if (currentRatio - screenRatio == 0f) {
                result = size
                break
            }
        }
        
        if (null == result) {
            for (size in pictureSizeList) {
                val curRatio = size.width.toFloat() / size.height
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size
                    break
                }
            }
        }
        
        return result
    }
    
//    private val shutterCallback = Camera.ShutterCallback { Log.d(TAG, "onShutter: shutter") }
//
//    private val rawCallback = Camera.PictureCallback { _, _ -> Log.d(TAG, "onPictureTaken: raw data") }
//
//    private lateinit var bitmap: Bitmap
//
//    private val jpegCallback = Camera.PictureCallback { data, _ ->
//        try {
//            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
//            //缩放
//            val matrix = Matrix()
//            matrix.postScale(0.5f, 0.5f)
//            bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
//            bmp.recycle()
//
//            val msg = Message.obtain()
//            msg.what = SET_IMAGE
//            msg.obj = bitmap
//            mHandler.sendMessage(msg)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun takePic() {
//        mCamera?.takePicture(shutterCallback, rawCallback, jpegCallback)
//    }
    
    fun getCameraData() {
        mCamera?.let {
            val parameters = it.parameters
            val floatArray = floatArrayOf(0f, 0f, 0f)
            parameters.getFocusDistances(floatArray)
            Log.d(TAG, "getCameraData: focus distances: ${floatArray[0]} ${floatArray[1]} ${floatArray[2]}")
            Log.d(TAG, "getCameraData: focalLenth: ${parameters.focalLength}")
            Log.d(TAG, "getCameraData: isZoomSupported: ${parameters.isZoomSupported}")
            Log.d(TAG, "getCameraData: zoom：${parameters.zoom}")
        }
    }
    
    fun onPermissionResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            ImageLoader.PERMISSIONS_CAMERA -> {
                //获得SD卡读写和打开相机权限
                val granted = (0 until grantResults.size).none {
                    grantResults[it] != PackageManager
                            .PERMISSION_GRANTED
                }
                if (granted) {
                    startCamera()
                } else {
                    Toast.makeText(activity, "打开相机失败！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    //todo 实现图片上传
//    fun uploadImage() {
//        OkHttpHelper.uploadImage(imageFilePath, object : Callback {
//            override fun onFailure(call: Call?, e: IOException?) {
//                val runnable = Runnable {
//                    e?.printStackTrace()
//                    Toast.makeText(activity, "上传图片失败！", Toast.LENGTH_SHORT).show()
//                }
//                handler.post(runnable)
//            }
//
//            override fun onResponse(call: Call?, response: Response?) {
//                val runnable = Runnable {
//                    Toast.makeText(activity, "上传图片成功", Toast.LENGTH_SHORT).show()
//                    try {
//                        val result: String = response?.body().toString()
//                        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show()
//                        Log.d(TAG, "onResponse: $result")
//                    } catch (e: NullPointerException) {
//                        e.printStackTrace()
//                    }
//                }
//                handler.post(runnable)
//            }
//        })
//    }
    
}
