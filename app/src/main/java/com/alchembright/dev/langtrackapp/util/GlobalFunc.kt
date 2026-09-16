package com.alchembright.dev.langtrackapp.util

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.alchembright.dev.langtrackapp.R

fun getVersionNumber(context: Context): String{
    var version = ""
    try {
        val pInfo =
            context.packageManager.getPackageInfo(context.packageName, 0)
        version = pInfo.versionName ?: ""

    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return version
}

fun showApiFailInfo(context: Context){
    Toast.makeText(context, context.getString(R.string.server_fail_info), Toast.LENGTH_SHORT).show()
}
