package com.nareshchocha.filepicker

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.ui.FilePicker

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FilePicker.isLoggingEnabled = true
        setContent {
            SetRootUI()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetRootUI() {
    val uriList = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val captureImageResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                uriList.clear()
                if (result.data?.data != null) {
                    result.data?.data?.let { uriList.add(it) }
                    Log.d("RESULT", "ActivityResult: ${result.data?.data}")
                } else {
                    val listData = result.data?.getClipDataUris()
                    Log.d("RESULT", "ActivityResult: $listData")
                    listData?.let { uriList.addAll(it) }
                }
                Log.d("RESULT", "ActivityResult Extras : ${result.data?.extras?.toString()}")
            } else {
                Log.e("RESULT", "ActivityResult: ${result.resultCode}")
            }
        }

    Scaffold(
        topBar = { TopAppBar(title = { Text("File Picker Sample") }) }
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
        ) {
            UriImageList(uriList)
            FilePickerButtons(context, captureImageResultLauncher)
        }
    }
}

@Composable
fun UriImageList(uriList: List<Uri>) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(100.dp)
    ) {
        items(uriList) { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(80.dp)
                        .padding(4.dp)
            )
        }
    }
}

@Composable
fun FilePickerButtons(
    context: Context,
    captureImageResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Spacer(modifier = Modifier.height(16.dp))
    CaptureImageButton(context, captureImageResultLauncher)
    Spacer(modifier = Modifier.height(8.dp))
    CaptureVideoButton(context, captureImageResultLauncher)
    Spacer(modifier = Modifier.height(8.dp))
    PickImagesButton(context, captureImageResultLauncher)
    Spacer(modifier = Modifier.height(8.dp))
    PickVideosButton(context, captureImageResultLauncher)
    Spacer(modifier = Modifier.height(8.dp))
    PickImagesAndVideosButton(context, captureImageResultLauncher)
    Spacer(modifier = Modifier.height(8.dp))
    PickPdfButton(context, captureImageResultLauncher)
    Spacer(modifier = Modifier.height(8.dp))
    AllOptionsButton(context, captureImageResultLauncher)
}

@Composable
fun CaptureImageButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker
                    .Builder(context)
                    .imageCaptureBuild(ImageCaptureConfig(isUseRearCamera = false))
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Capture Image") }
}

@Composable
fun CaptureVideoButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker.Builder(context).videoCaptureBuild()
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Capture Video") }
}

@Composable
fun PickImagesButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker.Builder(context).pickMediaBuild(
                    PickMediaConfig(mPickMediaType = PickMediaType.ImageOnly, allowMultiple = true)
                )
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Pick Images") }
}

@Composable
fun PickVideosButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker.Builder(context).pickMediaBuild(
                    PickMediaConfig(
                        mPickMediaType = PickMediaType.VideoOnly,
                        allowMultiple = true,
                        maxFiles = 3
                    )
                )
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Pick Videos") }
}

@Composable
fun PickImagesAndVideosButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker.Builder(context).pickMediaBuild(
                    PickMediaConfig(
                        mPickMediaType = PickMediaType.ImageAndVideo,
                        allowMultiple = true
                    )
                )
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Pick Images & Videos") }
}

@Composable
fun PickPdfButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker.Builder(context).pickDocumentFileBuild(
                    DocumentFilePickerConfig(
                        allowMultiple = true,
                        mMimeTypes = listOf("application/pdf")
                    )
                )
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Pick PDF") }
}
