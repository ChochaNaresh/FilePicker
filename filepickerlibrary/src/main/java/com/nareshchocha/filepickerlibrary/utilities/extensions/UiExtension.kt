package com.nareshchocha.filepickerlibrary.utilities.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity

private var cachedRequestedPermissions: Array<String>? = null

internal fun Context.getRequestedPermissions(): Array<String>? {
    if (cachedRequestedPermissions == null) {
        cachedRequestedPermissions =
            try {
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
            } catch (e: PackageManager.NameNotFoundException) {
                android.util.Log.e("UiExtension", "Package name not found", e)
                null
            }
    }
    return cachedRequestedPermissions
}

internal fun List<String>.asString() =
    this
        .map {
            it.split(".").lastOrNull() ?: ""
        }.toString()
        .let { listString ->
            listString.substring(1, listString.length - 1).replace(",", " and ")
        }

internal fun Context.getActivityOrNull(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}
