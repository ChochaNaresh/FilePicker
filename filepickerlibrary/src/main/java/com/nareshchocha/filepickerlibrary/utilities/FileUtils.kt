package com.nareshchocha.filepickerlibrary.utilities

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toUri
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extensions.copyFileToInternalStorage
import com.nareshchocha.filepickerlibrary.utilities.extensions.getDataColumn
import com.nareshchocha.filepickerlibrary.utilities.extensions.getDriveFilePath
import com.nareshchocha.filepickerlibrary.utilities.extensions.getMediaDocumentPath
import com.nareshchocha.filepickerlibrary.utilities.extensions.isDownloadsDocument
import com.nareshchocha.filepickerlibrary.utilities.extensions.isExternalStorageDocument
import com.nareshchocha.filepickerlibrary.utilities.extensions.isGoogleDriveUri
import com.nareshchocha.filepickerlibrary.utilities.extensions.isGooglePhotosUri
import com.nareshchocha.filepickerlibrary.utilities.extensions.isMediaDocument
import java.io.File

internal object FileUtils {
    /**
     * Resolves a [Uri] to a file-system path.
     *
     * @param resolveRealPath When `true` (default), the file is copied to internal storage as a
     *   last resort if no on-device path can be found. When `false`, copying is skipped — but any
     *   path that can be obtained **without** copying (e.g. a local Downloads file resolved via
     *   MediaStore) is still returned.
     */
    fun getRealPath(
        context: Context,
        fileUri: Uri,
        resolveRealPath: Boolean = true
    ): String? =
        try {
            pathFromURI(context, fileUri, resolveRealPath)
        } catch (e: IllegalArgumentException) {
            log(
                "IllegalArgumentException: ${e.message}, cannot get real path from URI: $fileUri",
                priority = LogPriority.ERROR_LOG,
                customTag = Const.LogTag.FILE_PICKER_ERROR,
                throwable = e
            )
            null
        } catch (e: SecurityException) {
            log(
                "SecurityException: ${e.message}, cannot get real path from URI: $fileUri",
                priority = LogPriority.ERROR_LOG,
                customTag = Const.LogTag.FILE_PICKER_ERROR,
                throwable = e
            )
            null
        }

    private fun pathFromURI(
        context: Context,
        uri: Uri,
        resolveRealPath: Boolean
    ): String? {
        val filePath =
            when {
                DocumentsContract.isDocumentUri(context, uri) -> getDocumentUri(context, uri, resolveRealPath)
                uri.isGoogleDriveUri() -> if (resolveRealPath) context.getDriveFilePath(uri) else null
                "content".equals(uri.scheme, ignoreCase = true) -> resolveContentUri(context, uri, resolveRealPath)
                "file".equals(uri.scheme, ignoreCase = true) -> uri.path
                else -> if (resolveRealPath) context.copyFileToInternalStorage(uri) else null
            }
        return verifyOrCopy(context, uri, filePath, resolveRealPath)
    }

    private fun resolveContentUri(
        context: Context,
        uri: Uri,
        resolveRealPath: Boolean
    ): String? =
        when {
            uri.isGooglePhotosUri() -> {
                uri.lastPathSegment
            }

            uri.isGoogleDriveUri() -> {
                if (resolveRealPath) context.getDriveFilePath(uri) else null
            }

            else -> {
                getDataColumn(context, uri, null, null)
                    ?: if (resolveRealPath) context.copyFileToInternalStorage(uri) else null
            }
        }

    private fun verifyOrCopy(
        context: Context,
        uri: Uri,
        filePath: String?,
        resolveRealPath: Boolean
    ): String? {
        if (filePath == null) return if (resolveRealPath) context.copyFileToInternalStorage(uri) else null
        val file = File(filePath)
        return if (file.canRead()) file.absolutePath else filePath
    }

    private fun getDocumentUri(
        context: Context,
        uri: Uri,
        resolveRealPath: Boolean
    ) = when {
        uri.isExternalStorageDocument() -> {
            getExternalDocumentPath(uri)
        }

        uri.isDownloadsDocument() -> {
            getFileName(context, uri)?.let {
                Environment.getExternalStorageDirectory().toString() + "/Download/" + it
            } ?: context.getDownloadsDocumentPath(uri)
                ?: if (resolveRealPath) context.copyFileToInternalStorage(uri) else null
        }

        uri.isMediaDocument() -> {
            context.getMediaDocumentPath(uri)
                ?: if (resolveRealPath) context.copyFileToInternalStorage(uri) else null
        }

        else -> {
            if (resolveRealPath) context.copyFileToInternalStorage(uri) else null
        }
    }

    private fun Context.getDownloadsDocumentPath(uri: Uri): String? {
        val id = DocumentsContract.getDocumentId(uri)
        val rawPath = id.removePrefix("raw:").takeIf { id.startsWith("raw:") && File(it).exists() }
        val mediaStorePath =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                id.toLongOrNull()?.let { queryMediaStoreDownloads(it) }
            } else {
                null
            }
        val legacyPath = queryLegacyDownloads(id)
        return rawPath ?: mediaStorePath ?: legacyPath
    }

    private fun Context.queryMediaStoreDownloads(longId: Long): String? =
        contentResolver
            .query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA),
                "${MediaStore.MediaColumns._ID}=?",
                arrayOf(longId.toString()),
                null
            )?.use { cursor ->
                cursor
                    .takeIf { it.moveToFirst() }
                    ?.getColumnIndex(MediaStore.MediaColumns.DATA)
                    ?.takeIf { it != -1 }
                    ?.let { cursor.getString(it) }
                    ?.takeIf { it.isNotEmpty() }
            }

    private fun Context.queryLegacyDownloads(id: String): String? =
        arrayOf(
            "content://downloads/public_downloads",
            "content://downloads/my_downloads"
        ).firstNotNullOfOrNull { prefix ->
            try {
                getDataColumn(this, ContentUris.withAppendedId(prefix.toUri(), java.lang.Long.valueOf(id)), null, null)
            } catch (e: NumberFormatException) {
                null
            }
        }

    private fun getExternalDocumentPath(uri: Uri): String {
        val docId = DocumentsContract.getDocumentId(uri)
        val split =
            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]

        return if ("primary".equals(type, ignoreCase = true)) {
            if (split.size > 1) {
                Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else {
                Environment.getExternalStorageDirectory().toString() + "/"
            }
        } else {
            "storage" + "/" + docId.replace(":", "/")
        }
    }

    private fun getFileName(
        context: Context,
        uri: Uri?
    ): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        return try {
            context.contentResolver
                .query(uri!!, projection, null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                        cursor.getString(index)
                    } else {
                        null
                    }
                }
        } catch (e: SecurityException) {
            log("SecurityException in getFileName", priority = LogPriority.ERROR_LOG, throwable = e)
            null
        } catch (e: IllegalArgumentException) {
            log("IllegalArgumentException in getFileName", priority = LogPriority.ERROR_LOG, throwable = e)
            null
        }
    }
}
