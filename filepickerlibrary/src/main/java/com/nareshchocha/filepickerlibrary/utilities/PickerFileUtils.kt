package com.nareshchocha.filepickerlibrary.utilities

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import java.io.File
import java.io.IOException

/**
 * Utility object that provides helper functions for file operations in the FilePicker library.
 * Contains methods for creating file directories, generating files, and obtaining content URIs.
 */
internal object PickerFileUtils {
    /**
     * Creates a file within the specified folder directory.
     *
     * @param folderFile The directory where the file should be created
     * @param fileName The name of the file to be created
     * @return A File object representing the newly created file path
     */
    fun createMediaFileFolder(
        folderFile: File,
        fileName: String
    ): File? =
        if (!folderFile.exists()) {
            try {
                if (!folderFile.mkdirs()) {
                    log(
                        "Failed to create directory: ${folderFile.path}",
                        priority = LogPriority.ERROR_LOG
                    )
                    null
                } else {
                    null
                }
            } catch (e: SecurityException) {
                log(
                    "Security exception creating directory: ${folderFile.path}",
                    priority = LogPriority.ERROR_LOG,
                    throwable = e
                )
                null
            }
        } else {
            try {
                File(folderFile.path + File.separator + fileName)
            } catch (e: SecurityException) {
                log(
                    "Security exception creating file: ${folderFile.path + File.separator + fileName}",
                    priority = LogPriority.ERROR_LOG,
                    throwable = e
                )
                null
            }
        }

    /**
     * Creates a file if it doesn't exist and returns its content URI.
     * The URI is created using a FileProvider for secure file sharing.
     *
     * @param mFile The File object to create and get URI for
     * @return A content URI for the file or null if creation or URI generation fails
     */
    fun Context.createFileGetUri(mFile: File?): Uri? {
        if (mFile == null) {
            log("File is null, cannot create or get URI", priority = LogPriority.ERROR_LOG)
            return null
        }
        var uri: Uri? = null
        if (!mFile.exists()) {
            try {
                mFile.createNewFile()
            } catch (e: IOException) {
                log(
                    "IO exception creating file: ${mFile.path}",
                    priority = LogPriority.ERROR_LOG,
                    throwable = e
                )
            } catch (e: SecurityException) {
                log(
                    "Security exception creating file: ${mFile.path}",
                    priority = LogPriority.ERROR_LOG,
                    throwable = e
                )
            }
        }
        if (mFile.exists()) {
            try {
                uri = getUriForFile(mFile)
            } catch (e: IOException) {
                log(
                    "IO exception getting URI for file: ${mFile.path}",
                    priority = LogPriority.ERROR_LOG,
                    throwable = e
                )
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
    private fun Context.getUriForFile(mFile: File): Uri? =
        FileProvider.getUriForFile(
            this.applicationContext,
            applicationContext.packageName +
                Const.AUTHORITY,
            mFile
        )
}
