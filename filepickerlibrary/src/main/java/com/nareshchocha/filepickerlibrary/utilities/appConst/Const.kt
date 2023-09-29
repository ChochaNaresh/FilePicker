package com.nareshchocha.filepickerlibrary.utilities.appConst

import android.content.Context
import android.os.Environment
import androidx.annotation.Keep
import com.nareshchocha.filepickerlibrary.R
import java.io.File

@Keep
object Const {
    internal const val CARD_RADIUS = 10f

    // internal const val AUTHORITY = "com.nareshchocha.filepickerlibrary.fileprovider"
    internal const val AUTHORITY = ".library.fileprovider"
    internal const val copyFileFolder = "copyFileToInternalStorage"


    internal object LogTag {
        const val FILE_RESULT = "FILE_RESULT ::"
        const val FILE_PICKER_ERROR = "FILE_PICKER_ERROR ::"
    }

    internal object DefaultPaths {
        fun defaultImageFile() = "tempImage_${System.currentTimeMillis()}.jpg"
        fun defaultVideoFile() = "tempVideo_${System.currentTimeMillis()}.mp4"
        fun Context.defaultFolder() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            getString(R.string.app_name),
        )
    }

    internal object BundleInternalExtras {
        const val PICKER_DATA = "PICKER_DATA"
        const val IMAGE_CAPTURE = "IMAGE_CAPTURE"
        const val VIDEO_CAPTURE = "VIDEO_CAPTURE"
        const val PICK_MEDIA = "PICK_MEDIA"
        const val PICK_DOCUMENT = "PICK_DOCUMENT"
    }

    @Keep
    object BundleExtras {
        const val FILE_PATH = "FILE_PATH"
        const val FILE_PATH_LIST = "FILE_PATH_LIST"
        const val ERROR = "ERROR"
    }
}
