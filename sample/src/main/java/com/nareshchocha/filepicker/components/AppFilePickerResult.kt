package com.nareshchocha.filepicker.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig

@Composable
fun AllFilePickers() {
    var pickedFiles by remember { mutableStateOf(listOf<PickedFile>()) }
    val context = LocalContext.current

    // Helper to add a file to the list
    fun addPickedFile(
        uri: Uri?,
        path: String?
    ) {
        uri ?: return
        val mimeType = context.contentResolver.getType(uri)
        val type =
            when {
                mimeType?.startsWith("image") == true -> "image"
                mimeType?.startsWith("video") == true -> "video"
                else -> "other"
            }
        pickedFiles = pickedFiles + PickedFile(uri, type, path)
    }

    val captureImageResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.ImageCapture()) { result ->
            addPickedFile(result?.selectedFileUri, result?.selectedFilePath)
        }
    val captureVideoResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.VideoCapture()) { result ->
            addPickedFile(result?.selectedFileUri, result?.selectedFilePath)
        }
    val pickImageResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.PickMedia()) { result ->
            addPickedFile(result?.selectedFileUri, result?.selectedFilePath)
        }
    val pickDocumentResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.PickDocumentFile()) { result ->
            addPickedFile(result?.selectedFileUri, result?.selectedFilePath)
        }
    val pickAllFilesResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.AllFilePicker()) { result ->
            addPickedFile(result?.selectedFileUri, result?.selectedFilePath)
        }
    val pickAnyFileResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.AnyFilePicker()) { result ->
            addPickedFile(result?.selectedFileUri, result?.selectedFilePath)
        }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppButton("Capture Image") { captureImageResultLauncher.launch(null) }
        AppButton("Capture Video") { captureVideoResultLauncher.launch(null) }
        AppButton("Pick Image") { pickImageResultLauncher.launch(null) }
        AppButton("Pick Document") { pickDocumentResultLauncher.launch(null) }
        AppButton("Pick All Files") { pickAllFilesResultLauncher.launch(null) }
        AppButton("Pick Any File") { pickAnyFileResultLauncher.launch(DocumentFilePickerConfig()) }

        if (pickedFiles.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Selected Files:", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            FilePickerWithResultList(pickedFiles)
        }
    }
}
