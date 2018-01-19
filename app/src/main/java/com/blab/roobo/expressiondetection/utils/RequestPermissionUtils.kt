package com.hellonurse.helloclient.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat

/**
 * Created by cfm on 2017/8/1.
 */

class RequestPermissionUtils {
    
    companion object {
        
        fun checkPermission(activity: Activity, permission: String): Boolean {
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                
                val checkSelfPermission = ActivityCompat.checkSelfPermission(activity, permission)
                return checkSelfPermission == PackageManager.PERMISSION_GRANTED
                
            }
            return true
        }
        
        /**
         * @return 返回是否需要动态申请权限
         */
        fun requestPermission(activity: Activity, permissions: ArrayList<String>, requestCode: Int,
                              explanation: String): Boolean {
            
            val TAG = "RequestPermissionUtils"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                
                //检查每个权限是否被授予过
                val iterator = permissions.iterator()
                while (iterator.hasNext()) {
                    val permission = iterator.next()
                    if (checkPermission(activity, permission)) {
                        iterator.remove()
                    }
                }
                
                if (permissions.size == 0) {
                    return false
                }
                
                //查看是否需要解释权限申请
                val shouldShow = (0..permissions.size - 1).any { ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[it]) }
                if (shouldShow) {
                    //解释为何需要权限
                    val dialog = AlertDialog.Builder(activity)
                            .setTitle("权限申请")
                            .setMessage(explanation)
                            .setPositiveButton("确定", { dialog, _ ->
                                ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), requestCode)
                                dialog.dismiss()
                            })
                            .create()
                    dialog.show()
                } else {
                    ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), requestCode)
                }
                
                return true
                
            }
            return false
            
        }
    }
}
