package com.blab.roobo.expressiondetection.network

import okhttp3.*
import java.io.File

/**
 * Created by memory4963 on 2018/1/17.
 */
class OkHttpHelper {
    
    companion object {
        
        val url = "http://121.48.158.70:10113/receive.php"
        
        val client: OkHttpClient = OkHttpClient()
        
        fun uploadImage(imageString: String, callback: Callback) {
            val request = Request.Builder()
                    .url(url)//地址
                    .header("User-Agent", "OkHttp Example")
                    .post(postBody(imageString))//添加请求体
                    .build()
            client.newCall(request).enqueue(callback)
        }
        
        protected fun postBody(srcPath: String): RequestBody {
            // 设置请求体
            val file = File(srcPath)
            val fileBody = RequestBody.create(MediaType.parse("image/png"), file)
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"uploadedfile\";filename=\"" + file.name + "\""), fileBody)
            return builder.build()
        }
        
    }
    
}
