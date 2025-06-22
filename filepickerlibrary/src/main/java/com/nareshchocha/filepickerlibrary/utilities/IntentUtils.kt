package com.nareshchocha.filepickerlibrary.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig

internal fun Context.getImageCaptureIntent(
    outputFileUri: Uri,
    useRearCamera: Boolean
): Intent =
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        val facingBack = if (useRearCamera) 1 else 0
        val facingFront = if (useRearCamera) 0 else 1
        val cameraFacing = if (useRearCamera) 0 else 1
        val useFrontCamera = !useRearCamera
        putExtra("android.intent.extras.LENS_FACING_BACK", facingBack)
        putExtra("android.intent.extras.LENS_FACING_FRONT", facingFront)
        putExtra("android.intent.extras.CAMERA_FACING", cameraFacing)
        putExtra("android.intent.extra.USE_FRONT_CAMERA", useFrontCamera)
    }

internal fun Context.getVideoCaptureIntent(
    outputFileUri: Uri,
    maxSeconds: Int? = null,
    maxSizeLimit: Long? = null,
    isHighQuality: Boolean? = null
): Intent =
    Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
        maxSeconds?.let { putExtra(MediaStore.EXTRA_DURATION_LIMIT, it) }
        isHighQuality?.let { putExtra(MediaStore.EXTRA_VIDEO_QUALITY, if (it) 1 else 0) }
        maxSizeLimit?.let { putExtra(MediaStore.EXTRA_SIZE_LIMIT, it) }
        flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
    }

internal fun getDocumentFilePick(config: DocumentFilePickerConfig): Intent =
    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, config.allowMultiple)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && config.allowMultiple == true) {
            putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, config.maxFiles)
        }
        config.mMimeTypes?.let { putExtra(Intent.EXTRA_MIME_TYPES, it.toTypedArray()) }
    }

internal fun Context.getMediaIntent(config: PickMediaConfig): Intent {
    val pickMediaType = config.getPickMediaType(config.mPickMediaType)
    val request =
        PickVisualMediaRequest(
            pickMediaType?.let { ActivityResultContracts.PickVisualMedia.SingleMimeType(it) }
                ?: ActivityResultContracts.PickVisualMedia.ImageAndVideo
        )
    return if (config.allowMultiple == true) {
        val maxFiles =
            config.maxFiles
                ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    MediaStore.getPickImagesMaxLimit()
                } else {
                    Int.MAX_VALUE
                }
        ActivityResultContracts.PickMultipleVisualMedia(maxFiles).createIntent(this, request)
    } else {
        ActivityResultContracts.PickVisualMedia().createIntent(this, request)
    }
}

internal fun Context.getSettingIntent(): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
