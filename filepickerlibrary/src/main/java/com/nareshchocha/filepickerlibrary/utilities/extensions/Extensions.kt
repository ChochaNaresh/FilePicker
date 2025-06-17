package com.nareshchocha.filepickerlibrary.utilities.extensions

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.nareshchocha.filepickerlibrary.utilities.LogPriority
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.log
import kotlin.text.substring

internal fun Context.getRequestedPermissions(): Array<String>? =
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

fun List<String>.asString() = this.map {
    it.split(".").lastOrNull() ?: ""
}.toString().let { listString ->
    listString.substring(1, listString.length - 1).replace(",", " and ")
}


// Helper extension for getting clip data URIs
internal fun Intent.getClipDataUris(): ArrayList<Uri> {
    val uris = mutableSetOf<Uri>()
    data?.let { uris.add(it) }
    val cd = clipData
    if (cd != null) {
        for (i in 0 until cd.itemCount) {
            cd.getItemAt(i).uri?.let { uris.add(it) }
        }
    }
    return ArrayList(uris)
}

internal fun Activity.setSuccessResult(
    fileUri: Uri?,
    filePath: String? = null,
    isFromCapture: Boolean = false
) {
    log("File Uri : $fileUri", LogPriority.INFO_LOG, Const.LogTag.FILE_PICKER_RESULT)
    log("filePath : $filePath", LogPriority.INFO_LOG, Const.LogTag.FILE_PICKER_RESULT)
    setResult(
        Activity.RESULT_OK,
        Intent().apply {
            flags =
                if (isFromCapture) {
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            fileUri?.let { data = it }
            if (isFromCapture) putExtra(Const.BundleExtras.FROM_CAPTURE, true)
            filePath?.let { putExtra(Const.BundleExtras.FILE_PATH, it) }
        }
    )
    finish()
}

internal fun Activity.setSuccessResult(
    fileUri: List<Uri>?,
    filePath: ArrayList<String>? = null,
    isFromCapture: Boolean = false
) {
    log("File Uri : $fileUri", LogPriority.INFO_LOG, Const.LogTag.FILE_PICKER_RESULT)
    log("filePath : $filePath", LogPriority.INFO_LOG, Const.LogTag.FILE_PICKER_RESULT)
    setResult(
        Activity.RESULT_OK,
        Intent().apply {
            flags =
                if (isFromCapture) {
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            if (isFromCapture) putExtra(Const.BundleExtras.FROM_CAPTURE, true)
            if (!fileUri.isNullOrEmpty()) {
                val mClipData = ClipData.newUri(contentResolver, "uris", fileUri.first())
                fileUri.drop(1).forEach { mClipData.addItem(ClipData.Item(it)) }
                clipData = mClipData
                filePath?.let { putStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST, it) }
            }
        }
    )
    finish()
}

internal fun Activity.setCanceledResult(error: String? = null) {
    log("Error: $error", LogPriority.ERROR_LOG, Const.LogTag.FILE_PICKER_RESULT)
    setResult(
        Activity.RESULT_CANCELED,
        Intent().apply { error?.let { putExtra(Const.BundleExtras.ERROR, it) } }
    )
    finish()
}

fun <T> List<T>.toArrayList(): ArrayList<T>? = ArrayList(this)
