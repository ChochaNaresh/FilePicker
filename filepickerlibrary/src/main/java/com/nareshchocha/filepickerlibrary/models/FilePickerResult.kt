package com.nareshchocha.filepickerlibrary.models

import android.net.Uri

data class FilePickerResult(
    val selectedFileUris: List<Uri>? = null,
    val selectedFilePaths: List<String>? = null,
    val selectedFileUri: Uri? = null,
    val selectedFilePath: String? = null,
    val isMultipleSelection: Boolean = false,
    val errorMessage: String? = null
)
