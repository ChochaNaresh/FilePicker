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
    var cursor: Cursor? = null
    val column = "_data"
    val projection =
        arrayOf(
            column
        )
    try {
        cursor =
            context.contentResolver.query(
                uri!!,
                projection,
                selection,
                selectionArgs,
                null
            )
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
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
    val returnCursor: Cursor? =
        contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )
    val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: return null
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    val file = File(cacheDir, sanitizeFileName(name))
    returnCursor.close()
    return try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        var read: Int?
        val bytesAvailable = inputStream?.available() ?: 0
        val bufferSize = bytesAvailable.coerceAtMost(Const.MAX_BUFFER_SIZE)
        val buffers = ByteArray(bufferSize)
        while (inputStream?.read(buffers).also { read = it } != -1) {
            read?.let { outputStream.write(buffers, 0, it) }
        }
        inputStream?.close()
        outputStream.close()
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
    val returnCursor: Cursor? =
        this.contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )
    val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: return null
    returnCursor.moveToFirst()
    val name = sanitizeFileName(returnCursor.getString(nameIndex))
    returnCursor.close()
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
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(output)
        var read: Int?
        val buffers = ByteArray(Const.BUFFER_SIZE)
        while (inputStream?.read(buffers).also { read = it } != -1) {
            read?.let { outputStream.write(buffers, 0, it) }
        }
        inputStream?.close()
        outputStream.close()
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
