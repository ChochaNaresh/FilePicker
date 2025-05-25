package com.nareshchocha.filepicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.Orientation
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.PopUpType
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import timber.log.Timber

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uriList = remember { mutableStateListOf<Uri>() }

            val captureImageResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        uriList.clear()
                        if (result.data?.data != null) {
                            result.data?.data?.let { uriList.add(it) }
                        } else {
                            val listData = result.data?.getClipDataUris()
                            listData?.let { uriList.addAll(it) }
                        }
                        Timber.tag("FILE_RESULT").v(result.toString())
                        Timber.tag("FILE_RESULT").v(result.data?.extras?.toString())
                    } else {
                        Timber.tag("FILE_PICKER_ERROR").v("capture Error")
                    }
                }

            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("File Picker Sample") })
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        items(uriList) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .imageCaptureBuild(ImageCaptureConfig(isUseRearCamera = false)),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Capture Image") }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .videoCaptureBuild(),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Capture Video") }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .pickMediaBuild(
                                        PickMediaConfig(
                                            mPickMediaType = PickMediaType.ImageOnly,
                                            allowMultiple = true,
                                        ),
                                    ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pick Images") }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .pickMediaBuild(
                                        PickMediaConfig(
                                            mPickMediaType = PickMediaType.VideoOnly,
                                            allowMultiple = true,
                                            maxFiles = 3,
                                        ),
                                    ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pick Videos") }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .pickMediaBuild(
                                        PickMediaConfig(
                                            mPickMediaType = PickMediaType.ImageAndVideo,
                                            allowMultiple = true,
                                        ),
                                    ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pick Images & Videos") }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .pickDocumentFileBuild(
                                        DocumentFilePickerConfig(
                                            allowMultiple = true,
                                            mMimeTypes = listOf("application/pdf"),
                                        ),
                                    ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pick PDF") }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            captureImageResultLauncher.launch(
                                FilePicker.Builder(this@MainActivity)
                                    .setPopUpConfig(
                                        PopUpConfig(
                                            mPopUpType = PopUpType.BOTTOM_SHEET,
                                            mOrientation = Orientation.VERTICAL,
                                            chooserTitle = "Choose Profile",
                                        ),
                                    )
                                    .addPickDocumentFile()
                                    .addImageCapture()
                                    .addVideoCapture()
                                    .addPickMedia()
                                    .build(),
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("All Options") }
                }
            }
        }
    }
}

// Helper extension for getting clip data URIs
fun Intent.getClipDataUris(): ArrayList<Uri> {
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data -> resultSet.add(data) }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return ArrayList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return ArrayList(resultSet)
}