package com.nareshchocha.filepickerlibray.utilities

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.nareshchocha.filepickerlibray.utilities.extentions.isDownloadsDocument
import com.nareshchocha.filepickerlibray.utilities.extentions.isExternalStorageDocument
import com.nareshchocha.filepickerlibray.utilities.extentions.isGooglePhotosUri
import com.nareshchocha.filepickerlibray.utilities.extentions.isMediaDocument
import java.io.File

internal object FileUtils {
    fun getRealPath(context: Context, fileUri: Uri): String? {
        return pathFromURI(context, fileUri)
    }

    private fun pathFromURI(context: Context, uri: Uri): String? {
        return when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                // ExternalStorageProvider
                when {
                    uri.isExternalStorageDocument() -> {
                        getExternalDocumentPath(uri)
                    }

                    uri.isDownloadsDocument() -> {
                        context.getDownloadsDocumentPath(uri)
                    }

                    uri.isMediaDocument() -> {
                        context.getMediaDocumentPath(uri)
                    }

                    else -> {
                        null
                    }
                }
            }

            "content".equals(uri.scheme, ignoreCase = true) -> {
                // Return the remote address
                if (uri.isGooglePhotosUri()) {
                    uri.lastPathSegment
                } else {
                    getDataColumn(
                        context,
                        uri,
                        null,
                        null,
                    )
                }
            }

            "file".equals(uri.scheme, ignoreCase = true) -> {
                uri.path
            }

            else -> {
                null
            }
        }
    }

    private fun Context.getMediaDocumentPath(uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split =
            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        var contentUri: Uri? = null
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
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(
            split[1],
        )
        return getDataColumn(this, contentUri, selection, selectionArgs)
    }

    private fun Context.getDownloadsDocumentPath(uri: Uri): String? {
        val fileName = getFilePath(this, uri)
        if (fileName != null) {
            Environment.getExternalStorageDirectory()
                .toString() + "/Download/" + fileName
        }
        var id = DocumentsContract.getDocumentId(uri)
        if (id.startsWith("raw:")) {
            id = id.replaceFirst("raw:".toRegex(), "")
            val file = File(id)
            if (file.exists()) return id
        }
        val contentUri = ContentUris.withAppendedId(
            Uri.parse("content://downloads/public_downloads"),
            java.lang.Long.valueOf(id),
        )
        return getDataColumn(this, contentUri, null, null)
    }

    private fun getExternalDocumentPath(uri: Uri): String {
        val docId = DocumentsContract.getDocumentId(uri)
        val split =
            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]

        // This is for checking Main Memory
        return if ("primary".equals(type, ignoreCase = true)) {
            if (split.size > 1) {
                Environment.getExternalStorageDirectory()
                    .toString() + "/" + split[1]
            } else {
                Environment.getExternalStorageDirectory().toString() + "/"
            }
            // This is for checking SD Card
        } else {
            "storage" + "/" + docId.replace(":", "/")
        }
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column,
        )
        try {
            cursor = context.contentResolver.query(
                uri!!,
                projection,
                selection,
                selectionArgs,
                null,
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

    private fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
        )
        try {
            cursor = context.contentResolver.query(
                uri!!,
                projection,
                null,
                null,
                null,
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}
