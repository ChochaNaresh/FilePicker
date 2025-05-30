package com.nareshchocha.filepickerlibrary.utilities.appConst

import android.content.Context
import android.os.Environment
import androidx.annotation.Keep
import com.nareshchocha.filepickerlibrary.R
import java.io.File


/**
 * Contains constants and utility objects used throughout the File Picker library.
 */
@Keep
object Const {
    /** Default card corner radius. */
    internal const val CARD_RADIUS = 10f

    /** FileProvider authority suffix. */
    internal const val AUTHORITY = ".library.fileprovider"

    /** Folder name for copying files to internal storage. */
    internal const val copyFileFolder = "copyFileToInternalStorage"

    /** Maximum buffer size for file operations (1 MB). */
    internal const val MAX_BUFFER_SIZE = 1 * 1024 * 1024

    /** Default buffer size for file operations (1 KB). */
    internal const val BUFFER_SIZE = 1024

    /**
     * Logging tags used for debugging and error reporting.
     */
    internal object LogTag {
        /** Tag for file result logs. */
        const val FILE_RESULT = "FILE_RESULT ::"
        /** Tag for file picker error logs. */
        const val FILE_PICKER_ERROR = "FILE_PICKER_ERROR ::"
        /** Tag for file picker exception logs. */
        const val FILE_PICKER_EXCEPTION = "FILE_PICKER_EXCEPTION :"
    }

    /**
     * Provides default file and folder paths for media operations.
     */
    internal object DefaultPaths {
        /**
         * Generates a default image file name with a timestamp.
         * @return The generated image file name.
         */
        fun defaultImageFile() = "tempImage_${System.currentTimeMillis()}.jpg"

        /**
         * Generates a default video file name with a timestamp.
         * @return The generated video file name.
         */
        fun defaultVideoFile() = "tempVideo_${System.currentTimeMillis()}.mp4"

        /**
         * Returns the default folder for storing files, located in the DCIM directory.
         * @receiver Context used to access resources.
         * @return The default folder as a [File] object.
         */
        fun Context.defaultFolder() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            getString(R.string.app_name)
        )
    }

    /**
     * Keys for internal bundle extras used within the library.
     */
    internal object BundleInternalExtras {
        /** Key for picker data. */
        const val PICKER_DATA = "PICKER_DATA"
        /** Key for image capture. */
        const val IMAGE_CAPTURE = "IMAGE_CAPTURE"
        /** Key for video capture. */
        const val VIDEO_CAPTURE = "VIDEO_CAPTURE"
        /** Key for picking media. */
        const val PICK_MEDIA = "PICK_MEDIA"
        /** Key for picking documents. */
        const val PICK_DOCUMENT = "PICK_DOCUMENT"
    }

    /**
     * Keys for bundle extras exposed to external consumers.
     */
    @Keep
    object BundleExtras {
        /** Indicates if the file is from capture. */
        const val FROM_CAPTURE = "isFromCapture"
        /** Key for a single file path. */
        const val FILE_PATH = "FILE_PATH"
        /** Key for a list of file paths. */
        const val FILE_PATH_LIST = "FILE_PATH_LIST"
        /** Key for error messages. */
        const val ERROR = "ERROR"
    }
}
