package com.nareshchocha.filepicker.components

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig

@Composable
fun FilePickerCaptureImage() {
    val captureImageResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.ImageCapture()) { result ->
            Log.d("FilePicker", "Image Capture Result: $result")
        }
    AppButton(
        text = "Capture Image",
        onClick = {
            captureImageResultLauncher.launch(null)
        }
    )
}

@Composable
fun FilePickerCaptureVideo() {
    val captureVideoResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.VideoCapture()) { result ->
            Log.d("FilePicker", "Video Capture Result: $result")
        }
    AppButton(
        text = "Capture Video",
        onClick = {
            captureVideoResultLauncher.launch(null)
        }
    )
}

@Composable
fun FilePickerPickImage() {
    val pickImageResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.PickMedia()) { result ->
            Log.d("FilePicker", "Pick Image Result: $result")
        }
    AppButton(
        text = "Pick Image",
        onClick = {
            pickImageResultLauncher.launch(null)
        }
    )
}

@Composable
fun FilePickerPickDocument() {
    val pickDocumentResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.PickDocumentFile()) { result ->
            Log.d("FilePicker", "Pick Document Result: $result")
        }
    AppButton(
        text = "Pick Document",
        onClick = {
            pickDocumentResultLauncher.launch(null)
        }
    )
}

@Composable
fun FilePickerAllFilePicker() {
    val pickDocumentResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.AllFilePicker()) { result ->
            Log.d("FilePicker", "Pick All Files Result: $result")
        }
    AppButton(
        text = "Pick All Files",
        onClick = {
            pickDocumentResultLauncher.launch(null)
        }
    )
}

@Composable
fun FilePickerAnyFilePicker() {
    val pickDocumentResultLauncher =
        rememberLauncherForActivityResult(FilePickerResultContracts.AnyFilePicker()) { result ->
            Log.d("FilePicker", "Pick Any File Result: $result")
        }
    AppButton(
        text = "Pick Any File",
        onClick = {
            pickDocumentResultLauncher.launch(DocumentFilePickerConfig())
        }
    )
}

@Preview
@Composable
fun AllFilePicker() {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilePickerCaptureImage()
        FilePickerCaptureVideo()
        FilePickerPickImage()
        FilePickerPickDocument()
        FilePickerAllFilePicker()
        FilePickerAnyFilePicker()
    }
}
