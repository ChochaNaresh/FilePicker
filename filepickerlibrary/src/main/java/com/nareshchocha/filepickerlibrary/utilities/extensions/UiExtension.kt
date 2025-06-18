package com.nareshchocha.filepickerlibrary.utilities.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

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

fun String?.toCapitalize(): String = this?.replaceFirstChar { it.uppercase() } ?: ""

@Composable
fun Modifier.noRippleClickable(): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {}
        )
    )
