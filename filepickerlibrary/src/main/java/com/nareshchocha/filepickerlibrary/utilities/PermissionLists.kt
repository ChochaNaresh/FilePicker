package com.nareshchocha.filepickerlibrary.utilities

import android.Manifest
import android.content.Context
import android.os.Build
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
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
    fun mediaFilePickerPermissions(config: PickMediaConfig): List<String> =
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when (config.mPickMediaType) {
                    PickMediaType.ImageOnly -> add(Manifest.permission.READ_MEDIA_IMAGES)
                    PickMediaType.VideoOnly -> add(Manifest.permission.READ_MEDIA_VIDEO)
                    PickMediaType.AudioOnly -> add(Manifest.permission.READ_MEDIA_AUDIO)
                    PickMediaType.ImageAndVideo -> {
                        add(Manifest.permission.READ_MEDIA_IMAGES)
                        add(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                    else -> {}
                }
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    // DocumentFilePicker permissions
    fun documentFilePickerPermissions(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            null
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}
