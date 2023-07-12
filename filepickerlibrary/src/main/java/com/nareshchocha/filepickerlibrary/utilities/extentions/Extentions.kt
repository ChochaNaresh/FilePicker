package com.nareshchocha.filepickerlibrary.utilities.extentions

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const

internal fun Context.showMyDialog(
    title: String,
    message: String,
    positiveButtonText: String? = null,
    negativeClick: () -> Unit,
    positiveClick: () -> Unit,
) {
    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setNegativeButton(
        getString(R.string.str_cancel),
    ) { dialog, _ ->
        negativeClick()
        dialog.dismiss()
    }
    builder.setPositiveButton(
        positiveButtonText ?: getString(R.string.str_ok),
    ) { dialog, _ ->
        positiveClick()
        dialog.dismiss()
    }
    builder.setOnCancelListener {
        negativeClick()
    }
    val alertDialog = builder.create()

    alertDialog.show()
}

internal fun Context.getImageCaptureIntent(outputFileUri: Uri): Intent {
    return Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
        it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        it.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
    }
}

internal fun Context.getVideoCaptureIntent(
    outputFileUri: Uri,
    maxSeconds: Int? = null,
    maxSizeLimit: Long? = null,
    isHighQuality: Boolean? = null,
): Intent {
    return Intent(MediaStore.ACTION_VIDEO_CAPTURE).also {
        maxSeconds?.let { seconds -> it.putExtra(MediaStore.EXTRA_DURATION_LIMIT, seconds) }
        isHighQuality?.let { isHighQuality ->
            it.putExtra(
                MediaStore.EXTRA_VIDEO_QUALITY,
                if (isHighQuality) 1 else 0,
            )
        }
        maxSizeLimit?.let { maxSizeLimit ->
            it.putExtra(
                MediaStore.EXTRA_SIZE_LIMIT,
                maxSizeLimit,
            )
        }
        it.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION + Intent.FLAG_GRANT_READ_URI_PERMISSION
        it.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            it.clipData = ClipData.newUri(contentResolver, "Video", outputFileUri)
        }*/
    }
}

internal fun getDocumentFilePick(mDocumentFilePickerConfig: DocumentFilePickerConfig): Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, mDocumentFilePickerConfig.allowMultiple)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (mDocumentFilePickerConfig.allowMultiple == true)
        ) {
            putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, mDocumentFilePickerConfig.maxFiles)
        }
        if (mDocumentFilePickerConfig.mMimeTypes != null) {
            putExtra(Intent.EXTRA_MIME_TYPES, mDocumentFilePickerConfig.mMimeTypes.toTypedArray())
        }
    }
}

internal fun Context.getMediaIntent(mPickMediaConfig: PickMediaConfig): Intent {
    val mPickMediaType = mPickMediaConfig.getPickMediaType(mPickMediaConfig.mPickMediaType)
    return if (mPickMediaConfig.allowMultiple == true) {
        ActivityResultContracts.PickMultipleVisualMedia(
            mPickMediaConfig.maxFiles
                ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    MediaStore.getPickImagesMaxLimit()
                } else {
                    Int.MAX_VALUE
                },
        ).createIntent(
            this,
            PickVisualMediaRequest(
                if (mPickMediaType != null) {
                    ActivityResultContracts.PickVisualMedia.SingleMimeType(
                        mPickMediaType,
                    )
                } else {
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
                },
            ),
        )
    } else {
        ActivityResultContracts.PickVisualMedia().createIntent(
            this,
            PickVisualMediaRequest(
                if (mPickMediaType != null) {
                    ActivityResultContracts.PickVisualMedia.SingleMimeType(
                        mPickMediaType,
                    )
                } else {
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
                },
            ),
        )
    }
}

internal fun Context.getSettingIntent(): Intent {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    return intent
}

internal fun Activity.setSuccessResult(
    fileUri: Uri?,
    filePath: String? = null,
    isFromCapture: Boolean = false,
) {
    setResult(
        Activity.RESULT_OK,
        Intent().also { mIntent ->
            mIntent.flags =
                if (isFromCapture) {
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION + Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

            fileUri?.let { mIntent.data = fileUri }
            filePath?.let { mIntent.putExtra(Const.BundleExtras.FILE_PATH, it) }
        },
    )
    finish()
}

internal fun Activity.setSuccessResult(
    fileUri: List<Uri>?,
    filePath: ArrayList<String>? = null,
    isFromCapture: Boolean = false,
) {
    setResult(
        Activity.RESULT_OK,
        Intent().also { mIntent ->
            mIntent.flags =
                if (isFromCapture) {
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION + Intent.FLAG_GRANT_READ_URI_PERMISSION
                } else {
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
            if (!fileUri.isNullOrEmpty()) {
                val mClipData = ClipData.newUri(contentResolver, "uris", fileUri.first())
                fileUri.subList(1, fileUri.size).forEach {
                    mClipData.addItem(ClipData.Item(it))
                }
                mIntent.clipData = mClipData
                filePath?.let {
                    mIntent.putStringArrayListExtra(
                        Const.BundleExtras.FILE_PATH_LIST,
                        it,
                    )
                }
            }
        },
    )
    finish()
}

internal fun Activity.setCanceledResult(error: String? = null) {
    setResult(
        Activity.RESULT_CANCELED,
        Intent().also { mIntent ->
            error?.let { mIntent.putExtra(Const.BundleExtras.ERROR, it) }
        },
    )
    finish()
}
