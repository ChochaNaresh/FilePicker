package com.nareshchocha.filepicker.components

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepicker.R
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.FilePickerResult
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.Orientation
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.PopUpType
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig

@Composable
fun AllFilePickers() {
    var pickedFiles by remember { mutableStateOf(listOf<PickedFile>()) }
    val context = LocalContext.current
    val sections =
        rememberPickerSections { result ->
            val new = buildPickedFiles(context, result)
            if (new.isNotEmpty()) pickedFiles = pickedFiles + new
        }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sections.forEach { section ->
            item(key = "header_${section.title}") { SectionHeader(section.title) }
            items(section.buttons, key = { "btn_${it.text}" }) { AppButton(it.text, it.onClick) }
        }

        if (pickedFiles.isNotEmpty()) {
            item(key = "results_header") {
                PickedFilesHeader(pickedFiles.size) { pickedFiles = emptyList() }
            }
            item(key = "tap_hint") {
                Text(
                    text = stringResource(R.string.tap_to_open_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(pickedFiles, key = { it.uuid }) { FileItem(it) }
        }
    }
}

private fun buildPickedFiles(
    context: Context,
    result: FilePickerResult
): List<PickedFile> {
    if (!result.isSuccess()) return emptyList()
    val uris = result.selectedFileUris
    val uri = result.selectedFileUri
    return when {
        uris != null -> {
            uris.mapIndexed { i, u ->
                val mt = runCatching { context.contentResolver.getType(u) }.getOrNull()
                PickedFile(u, mt.toFileType(), mt, result.selectedFilePaths?.getOrNull(i))
            }
        }

        uri != null -> {
            val mt = runCatching { context.contentResolver.getType(uri) }.getOrNull()
            listOf(PickedFile(uri, mt.toFileType(), mt, result.selectedFilePath))
        }

        else -> {
            emptyList()
        }
    }
}

private fun String?.toFileType() =
    when {
        this?.startsWith("image") == true -> "image"
        this?.startsWith("video") == true -> "video"
        else -> "other"
    }

@Composable
private fun rememberPickerSections(onResult: (FilePickerResult) -> Unit): List<PickerSection> {
    val context = LocalContext.current
    val imgLauncher =
        rememberLauncherForActivityResult(
            FilePickerResultContracts.ImageCapture()
        ) { onResult(it) }
    val vidLauncher =
        rememberLauncherForActivityResult(
            FilePickerResultContracts.VideoCapture()
        ) { onResult(it) }
    val mediaLauncher =
        rememberLauncherForActivityResult(
            FilePickerResultContracts.PickMedia()
        ) { onResult(it) }
    val docLauncher =
        rememberLauncherForActivityResult(
            FilePickerResultContracts.PickDocumentFile()
        ) { onResult(it) }
    val allLauncher =
        rememberLauncherForActivityResult(
            FilePickerResultContracts.AllFilePicker()
        ) { onResult(it) }
    val anyLauncher =
        rememberLauncherForActivityResult(
            FilePickerResultContracts.AnyFilePicker()
        ) { onResult(it) }
    return listOf(
        cameraSection(context, imgLauncher, vidLauncher),
        mediaSection(context, mediaLauncher),
        documentsSection(context, docLauncher),
        popupSection(context, allLauncher),
        anyPickerSection(context, anyLauncher)
    )
}

private fun cameraSection(
    context: Context,
    imgLauncher: ActivityResultLauncher<ImageCaptureConfig?>,
    vidLauncher: ActivityResultLauncher<VideoCaptureConfig?>
) = PickerSection(
    context.getString(R.string.section_camera),
    listOf(
        PickerButton(context.getString(R.string.btn_capture_image_rear)) {
            imgLauncher.launch(ImageCaptureConfig(isUseRearCamera = true))
        },
        PickerButton(context.getString(R.string.btn_capture_image_front)) {
            imgLauncher.launch(ImageCaptureConfig(isUseRearCamera = false))
        },
        PickerButton(context.getString(R.string.btn_capture_video)) {
            vidLauncher.launch(VideoCaptureConfig())
        },
        PickerButton(context.getString(R.string.btn_capture_video_hq)) {
            vidLauncher.launch(VideoCaptureConfig(maxSeconds = 30, isHighQuality = true))
        }
    )
)

private fun mediaSection(
    context: Context,
    mediaLauncher: ActivityResultLauncher<PickMediaConfig?>
) = PickerSection(
    context.getString(R.string.section_media_gallery),
    listOf(
        PickerButton(context.getString(R.string.btn_pick_single_image)) {
            mediaLauncher.launch(PickMediaConfig(mPickMediaType = PickMediaType.ImageOnly))
        },
        PickerButton(context.getString(R.string.btn_pick_single_video)) {
            mediaLauncher.launch(PickMediaConfig(mPickMediaType = PickMediaType.VideoOnly))
        },
        PickerButton(context.getString(R.string.btn_pick_multiple_media)) {
            mediaLauncher.launch(
                PickMediaConfig(mPickMediaType = PickMediaType.ImageAndVideo, allowMultiple = true)
            )
        }
    )
)

private fun documentsSection(
    context: Context,
    docLauncher: ActivityResultLauncher<DocumentFilePickerConfig?>
) = PickerSection(
    context.getString(R.string.section_documents),
    listOf(
        PickerButton(context.getString(R.string.btn_pick_any_document)) {
            docLauncher.launch(DocumentFilePickerConfig())
        },
        PickerButton(context.getString(R.string.btn_pick_pdf_only)) {
            docLauncher.launch(DocumentFilePickerConfig(mMimeTypes = listOf("application/pdf")))
        },
        PickerButton(context.getString(R.string.btn_pick_multiple_documents)) {
            docLauncher.launch(DocumentFilePickerConfig(allowMultiple = true))
        }
    )
)

private fun popupSection(
    context: Context,
    allLauncher: ActivityResultLauncher<PickerData?>
) = PickerSection(
    context.getString(R.string.section_popup_pickers),
    listOf(
        PickerButton(context.getString(R.string.btn_all_bottom_sheet_vertical)) {
            allLauncher.launch(
                PickerData(
                    mPopUpConfig =
                        PopUpConfig(
                            mPopUpType = PopUpType.BOTTOM_SHEET,
                            mOrientation = Orientation.VERTICAL
                        ),
                    listIntents =
                        listOf(
                            ImageCaptureConfig(),
                            VideoCaptureConfig(),
                            PickMediaConfig(),
                            DocumentFilePickerConfig()
                        )
                )
            )
        },
        PickerButton(context.getString(R.string.btn_all_bottom_sheet_horizontal)) {
            allLauncher.launch(
                PickerData(
                    mPopUpConfig =
                        PopUpConfig(
                            mPopUpType = PopUpType.BOTTOM_SHEET,
                            mOrientation = Orientation.HORIZONTAL
                        ),
                    listIntents =
                        listOf(
                            ImageCaptureConfig(),
                            VideoCaptureConfig(),
                            PickMediaConfig(),
                            DocumentFilePickerConfig()
                        )
                )
            )
        },
        PickerButton(context.getString(R.string.btn_all_dialog)) {
            allLauncher.launch(
                PickerData(
                    mPopUpConfig =
                        PopUpConfig(
                            mPopUpType = PopUpType.DIALOG,
                            mOrientation = Orientation.VERTICAL
                        ),
                    listIntents =
                        listOf(
                            ImageCaptureConfig(),
                            VideoCaptureConfig(),
                            PickMediaConfig(),
                            DocumentFilePickerConfig()
                        )
                )
            )
        }
    )
)

private fun anyPickerSection(
    context: Context,
    anyLauncher: ActivityResultLauncher<BaseConfig?>
) = PickerSection(
    context.getString(R.string.section_any_file_picker),
    listOf(
        PickerButton(context.getString(R.string.btn_any_capture_image)) {
            anyLauncher.launch(ImageCaptureConfig())
        },
        PickerButton(context.getString(R.string.btn_any_pick_document)) {
            anyLauncher.launch(DocumentFilePickerConfig())
        }
    )
)

@Composable
private fun PickedFilesHeader(
    count: Int,
    onClear: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.selected_files_count, count),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        TextButton(onClick = onClear) { Text(stringResource(R.string.clear_all)) }
    }
}

class PickerButton(
    val text: String,
    val onClick: () -> Unit
)

data class PickerSection(
    val title: String,
    val buttons: List<PickerButton>
)
