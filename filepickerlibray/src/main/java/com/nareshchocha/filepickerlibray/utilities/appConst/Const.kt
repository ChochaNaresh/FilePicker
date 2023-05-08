package com.nareshchocha.filepickerlibray.utilities.appConst

import android.os.Environment
import java.io.File

object Const {
    internal const val CARD_RADIUS = 10f

    internal object LogTag {
        const val FILE_RESULT = "FILE_RESULT ::"
        const val FILE_PICKER_ERROR = "FILE_PICKER_ERROR ::"
    }

    internal object DefaultPaths {
        val defaultFolder: File = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "FilePicker",
        )
        val defaultImageFile = "tempImage_${System.currentTimeMillis()}.jpg"
        val defaultVideoFile = "tempVideo_${System.currentTimeMillis()}.mp4"
    }

    internal object BundleInternalExtras {
        const val PICKER_DATA = "PICKER_DATA"
        const val IMAGE_CAPTURE = "IMAGE_CAPTURE"
        const val VIDEO_CAPTURE = "VIDEO_CAPTURE"
        const val PICK_MEDIA = "PICK_MEDIA"
        const val PICK_DOCUMENT = "PICK_DOCUMENT"
    }
    object BundleExtras {
        const val FILE_PATH = "FILE_PATH"
        const val FILE_PATH_LIST = "FILE_PATH_LIST"
        const val ERROR = "ERROR"
    }
}
