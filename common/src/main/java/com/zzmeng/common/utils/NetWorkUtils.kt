package com.zzmeng.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager

class NetWorkUtils {
    companion object {
        @SuppressLint("MissingPermission")
        fun isNetworkConnected(context: Context) : Boolean {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return manager.activeNetworkInfo != null && manager.activeNetworkInfo.isAvailable
        }
    }
}