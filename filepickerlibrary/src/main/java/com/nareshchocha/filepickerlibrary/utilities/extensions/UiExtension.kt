package com.nareshchocha.filepickerlibrary.utilities.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity

fun Context.getRequestedPermissions(): Array<String>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
        )
    } else {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_PERMISSIONS
        )
    }.requestedPermissions

fun Context.getActivityOrNull(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}
