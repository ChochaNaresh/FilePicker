package com.nareshchocha.filepickerlibrary.utilities

import android.Manifest
import android.content.Context
import android.os.Build
import com.nareshchocha.filepickerlibrary.utilities.extensions.getRequestedPermissions

internal object PermissionLists {
    // ImageCapture and VideoCapture use the same permissions
    fun captureMediaPermissions(context: Context): List<String> =
        buildList {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            context.getRequestedPermissions()?.let {
                if (it.contains(Manifest.permission.CAMERA)) {
                    add(Manifest.permission.CAMERA)
                }
            }
        }

    // Aliases for backward compatibility
    fun imageCapturePermissions(context: Context): List<String> = captureMediaPermissions(context)

    fun videoCapturePermissions(context: Context): List<String> = captureMediaPermissions(context)

    // MediaFilePicker permissions
    fun mediaFilePickerPermissions(): List<String> =
        buildList {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    // DocumentFilePicker permissions
    fun documentFilePickerPermissions(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            null
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}
