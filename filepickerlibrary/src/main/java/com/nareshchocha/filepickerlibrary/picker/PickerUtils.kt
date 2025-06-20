package com.nareshchocha.filepickerlibrary.picker

import android.content.Context
import android.net.Uri
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Utility object that provides helper functions for file operations in the FilePicker library.
 * Contains methods for creating file directories, generating files, and obtaining content URIs.
 */
internal object PickerUtils {

    /**
     * Creates a file within the specified folder directory.
     *
     * @param folderFile The directory where the file should be created
     * @param fileName The name of the file to be created
     * @return A File object representing the newly created file path
     */
    @Keep
    fun createMediaFileFolder(
        folderFile: File,
        fileName: String,
    ): File {
        if (!folderFile.exists()) {
            try {
                if (!folderFile.mkdirs()) {
                    Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                        .e("Failed to create directory: ${folderFile.path}")
                }
            } catch (e: SecurityException) {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .e(e, "Security exception creating directory: ${folderFile.path}")
            }
        }
        return File(folderFile.path + File.separator + fileName)
    }

    /**
     * Creates a file if it doesn't exist and returns its content URI.
     * The URI is created using a FileProvider for secure file sharing.
     *
     * @param mFile The File object to create and get URI for
     * @return A content URI for the file or null if creation or URI generation fails
     */
    @Keep
    fun Context.createFileGetUri(mFile: File): Uri? {
        var uri: Uri? = null
        if (!mFile.exists()) {
            try {
                mFile.createNewFile()
            } catch (e: IOException) {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .e(e, "IO exception creating file: ${mFile.path}")
            } catch (e: SecurityException) {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .e(e, "Security exception creating file: ${mFile.path}")
            }
        }
        if (mFile.exists()) {
            try {
                uri = getUriForFile(mFile)
            } catch (e: IOException) {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .e(e, "Failed to get URI for file: ${mFile.path}")
            }
        }
        return uri
    }

    /**
     * Gets a content URI for a file using FileProvider.
     *
     * @param mFile The File object to get a URI for
     * @return A content URI for the file
     * @throws IllegalArgumentException if the file cannot be shared
     */
    private fun Context.getUriForFile(mFile: File): Uri? {
        return FileProvider.getUriForFile(
            this.applicationContext,
            applicationContext.packageName +
                    Const.AUTHORITY,
            mFile,
        )
    }
}
