package com.nareshchocha.filepickerlibrary.utilities.extensions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.nareshchocha.filepickerlibrary.utilities.LogPriority
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
internal fun Uri.isExternalStorageDocument(): Boolean = "com.android.externalstorage.documents" == authority

/**
 * @return Whether the Uri authority is DownloadsProvider.
 */
internal fun Uri.isDownloadsDocument(): Boolean = "com.android.providers.downloads.documents" == authority

/**
 * @return Whether the Uri authority is MediaProvider.
 */
internal fun Uri.isMediaDocument(): Boolean = "com.android.providers.media.documents" == authority

/**
 * @return Whether the Uri authority is Google Photos.
 */
internal fun Uri.isGooglePhotosUri(): Boolean = "com.google.android.apps.photos.content" == authority

internal fun Uri.isGoogleDriveUri(): Boolean =
    "com.google.android.apps.docs.storage" == authority ||
        "com.google.android.apps.docs.storage.legacy" == authority

internal fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    val column = "_data"
    val projection = arrayOf(column)
    return try {
        context.contentResolver
            .query(
                uri!!,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    cursor.getString(index)
                } else {
                    null
                }
            }
    } catch (e: SecurityException) {
        log("SecurityException in getDataColumn", priority = LogPriority.ERROR_LOG, throwable = e)
        null
    } catch (e: IllegalArgumentException) {
        log("IllegalArgumentException in getDataColumn", priority = LogPriority.ERROR_LOG, throwable = e)
        null
    }
}

internal fun Context.getMediaDocumentPath(uri: Uri): String? {
    val docId = DocumentsContract.getDocumentId(uri)
    val split =
        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val type = split[0]
    val contentUri: Uri?
    when (type) {
        "image" -> {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        "video" -> {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        "audio" -> {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        else -> {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }
    val selection = "_id=?"
    val selectionArgs =
        arrayOf(
            split[1]
        )
    return getDataColumn(this, contentUri, selection, selectionArgs)
}

internal fun Context.getDriveFilePath(uri: Uri): String? {
    val name =
        contentResolver
            .query(
                uri,
                null,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) cursor.getString(nameIndex) else null
                } else {
                    null
                }
            } ?: "drive_file_${System.currentTimeMillis()}"

    val file = File(cacheDir, sanitizeFileName(name))
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.path
    } catch (e: IOException) {
        log(
            "Error file from Google Drive: ${e.message}",
            priority = LogPriority.ERROR_LOG,
            throwable = e
        )
        null
    }
}

/**
 * Sanitizes a filename to prevent directory traversal attacks and other security issues
 */
private fun sanitizeFileName(fileName: String): String {
    // Remove any path components that could cause directory traversal
    val name = fileName.replace("[\\\\/:*?\"<>|]".toRegex(), "_")

    // Limit filename length if needed
    return if (name.length > FILE_NAME_LENGTH) {
        val extension = name.substringAfterLast('.', "")
        val baseName = name.substringBeforeLast('.')
        val maxBaseLength =
            FILE_NAME_LENGTH - extension.length - (if (extension.isNotEmpty()) 1 else 0)
        if (extension.isNotEmpty()) {
            "${baseName.take(maxBaseLength)}.$extension"
        } else {
            baseName.take(maxBaseLength)
        }
    } else {
        name
    }
}

const val FILE_NAME_LENGTH = 255

/***
 * Used for Android Q+
 * @param uri
 * @param newDirName if you want to create a directory, you can set this variable
 * @return
 */
internal fun Context.copyFileToInternalStorage(
    uri: Uri,
    newDirName: String = Const.COPY_FILE_FOLDER
): String? {
    val name =
        this.contentResolver
            .query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) sanitizeFileName(cursor.getString(nameIndex)) else null
                } else {
                    null
                }
            } ?: "picked_file_${System.currentTimeMillis()}"

    val output: File =
        if (newDirName != "") {
            val dir = File(cacheDir, newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            File(cacheDir, "$newDirName/$name")
        } else {
            File(cacheDir, "/$name")
        }
    return try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(output).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        output.path
    } catch (e: IOException) {
        log(
            "Error file to internal storage: ${e.message}",
            priority = LogPriority.ERROR_LOG,
            throwable = e
        )
        output.delete()
        null
    }
}
