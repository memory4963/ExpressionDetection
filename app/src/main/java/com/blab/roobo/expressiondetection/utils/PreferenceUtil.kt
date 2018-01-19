package com.hellonurse.helloclient.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.blab.roobo.expressiondetection.BuildConfig
import com.blab.roobo.expressiondetection.base.App


/**
 * Created by cfm on 2017/7/30.
 */
class PreferenceUtil {
    
    companion object {
        
        private val TAG = "PreferenceUtil"
        
        private val PREFERENCES_NAME = "shared_preferences"
        val TOKEN = "token"
        val REFRESH_TOKEN = "refresh_token"
        val USER_TEL = "user_tel"
        val USER_ID = "user_id"
        val HEAD_IMAGE_URL = "head_image_path"
        val PUSHABLE = "pushable"
        val UNPUSHABLE = "unpushable"
        
        private var sharedPreferences: SharedPreferences? = null
        
        @Synchronized private fun init() {
            sharedPreferences = App.getInstance().getSharedPreferences(PREFERENCES_NAME, Context
                    .MODE_PRIVATE)
        }
        
        fun get(key: String): String {
            if (sharedPreferences == null) {
                init()
            }
            return sharedPreferences!!.getString(key, "")
        }
        
        fun put(key: String, data: String) {
            if (sharedPreferences == null) {
                init()
            }
            sharedPreferences!!.edit().putString(key, data).apply()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "put: 更新$key:$data")
            }
        }
    }
}
