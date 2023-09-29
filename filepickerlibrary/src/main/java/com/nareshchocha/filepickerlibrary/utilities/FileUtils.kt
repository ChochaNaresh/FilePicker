package com.nareshchocha.filepickerlibrary.utilities

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.Keep
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.isDownloadsDocument
import com.nareshchocha.filepickerlibrary.utilities.extentions.isExternalStorageDocument
import com.nareshchocha.filepickerlibrary.utilities.extentions.isGoogleDriveUri
import com.nareshchocha.filepickerlibrary.utilities.extentions.isGooglePhotosUri
import com.nareshchocha.filepickerlibrary.utilities.extentions.isMediaDocument
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Keep
internal object FileUtils {
    @Keep
    fun getRealPath(context: Context, fileUri: Uri): String? {
        return pathFromURI(context, fileUri)
    }

    @Keep
    private fun pathFromURI(context: Context, uri: Uri): String? {
        return when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                // ExternalStorageProvider
                when {
                    uri.isExternalStorageDocument() -> {
                        getExternalDocumentPath(uri)
                    }

                    uri.isDownloadsDocument() -> {
                        context.getDownloadsDocumentPath(uri) ?: context.copyFileToInternalStorage(
                            uri
                        )
                    }

                    uri.isMediaDocument() -> {
                        context.getMediaDocumentPath(uri) ?: context.copyFileToInternalStorage(uri)
                    }

                    else -> {
                        context.copyFileToInternalStorage(uri)
                    }
                }
            }

            uri.isGoogleDriveUri() -> {
                context.getDriveFilePath(uri);
            }

            "content".equals(uri.scheme, ignoreCase = true) -> {
                // Return the remote address
                if (uri.isGooglePhotosUri()) {
                    uri.lastPathSegment
                } else if (uri.isGoogleDriveUri()) {
                    return context.getDriveFilePath(uri);
                } else {
                    getDataColumn(
                        context,
                        uri,
                        null,
                        null,
                    ) ?: context.copyFileToInternalStorage(uri)
                }
            }

            "file".equals(uri.scheme, ignoreCase = true) -> {
                uri.path
            }

            else -> {
                context.copyFileToInternalStorage(uri)
            }
        }
    }


    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private fun Context.copyFileToInternalStorage(
        uri: Uri?,
        newDirName: String = Const.copyFileFolder
    ): String? {
        if (uri == null) {
            return null
        }
        val returnCursor: Cursor? = this.contentResolver.query(
            uri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: return null
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val output: File = if (newDirName != "") {
            val dir = File(cacheDir, newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            File(cacheDir, "$newDirName/$name")
        } else {
            File(cacheDir, "/$name")
        }
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read: Int? = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also { read = it } != -1) {
                read?.let { outputStream.write(buffers, 0, it) }
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            output.delete()
            Timber.tag("Exception").e(e.message!!)
            return null
        }
        return output.path
    }


    private fun Context.getDriveFilePath(uri: Uri): String? {
        val returnCursor: Cursor? = contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: return null
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(cacheDir, name)
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int? = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream?.available() ?: 0

            //int bufferSize = 1024;
            val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also { read = it } != -1) {
                read?.let { outputStream.write(buffers, 0, it) }
            }
            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            Timber.tag("Exception").e(e.message)
            return null
        }
        return file.path
    }

    @Keep
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

            else -> {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(
            split[1],
        )
        return getDataColumn(this, contentUri, selection, selectionArgs)
    }

    @Keep
    private fun Context.getDownloadsDocumentPath(uri: Uri): String? {
        val fileName = getFilePath(this, uri)
        if (fileName != null) {
            return Environment.getExternalStorageDirectory()
                .toString() + "/Download/" + fileName
        }
        var id = DocumentsContract.getDocumentId(uri)
        if (id.startsWith("raw:")) {
            id = id.replaceFirst("raw:".toRegex(), "")
            val file = File(id)
            if (file.exists()) return id
        }
        val contentUriPrefixesToTry = arrayOf(
            "content://downloads/public_downloads",
            "content://downloads/my_downloads"
        )
        for (contentUriPrefix in contentUriPrefixesToTry) {
            return try {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse(contentUriPrefix),
                    java.lang.Long.valueOf(id)
                )
                getDataColumn(this, contentUri, null, null)
            } catch (e: NumberFormatException) {
                //In Android 8 and Android P the id is not a number
                uri.path!!.replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "")
            }
        }
        return null
    }

    @Keep
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

    @Keep
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
            Timber.tag("Checked:").d("cursor:: %s", cursor)

            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    @Keep
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
