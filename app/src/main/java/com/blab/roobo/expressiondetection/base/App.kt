package com.blab.roobo.expressiondetection.base

import android.app.Application

/**
 * Created by memory4963 on 2018/1/17.
 */
class App : Application() {
    
    init {
        app = this
    }
    
    companion object {
    
        private var app: App? = null
    
        fun getInstance(): App {
            return app!!
        }
        
    }
    
}
