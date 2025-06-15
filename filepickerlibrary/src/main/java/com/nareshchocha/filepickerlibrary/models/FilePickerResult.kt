package com.nareshchocha.filepickerlibrary.models

import android.net.Uri

data class FilePickerResult(
    val selectedFileUri: Uri? = null,
    val selectedFilePath: String? = null,
    val selectedFileUris: List<Uri?>? = null,
    val selectedFilePaths: List<String?>? = null,
    val errorMessage: String? = null
) {
    fun isSuccess(): Boolean = errorMessage.isNullOrEmpty()
}
