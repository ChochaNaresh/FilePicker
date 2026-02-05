package com.nareshchocha.filepicker.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.util.UUID

@Composable
fun FilePickerWithResultList(pickedFiles: List<PickedFile>) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(items = pickedFiles, key = { it.uuid }) { file ->
            FileItem(file = file)
        }
    }
}

@Composable
private fun FileItem(file: PickedFile) {
    when (file.type) {
        "image" -> ImageItem(uri = file.uri)
        "video" -> VideoItem(uri = file.uri)
        else -> OtherFileItem(file = file)
    }
}

@Composable
private fun ImageItem(uri: Uri) {
    CoilImage(
        imageModel = { uri }, // loading a network image or local resource using an URL.
        imageOptions =
            ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            ),
        modifier = Modifier.size(100.dp)
    )
}

@Composable
private fun VideoItem(uri: Uri) {
    Row(
        modifier =
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ).padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "Video",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "Video: $uri",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun OtherFileItem(file: PickedFile) {
    Column(
        modifier =
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                ).padding(12.dp)
    ) {
        Text(
            text = "URI: ${file.uri}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Path: ${file.filePath ?: "N/A"}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Immutable
data class PickedFile(
    val uri: Uri,
    val type: String, // "image", "video", "other"
    val filePath: String? = null,
    val uuid: String = UUID.randomUUID().toString()
)

@Preview
@Composable
fun PreviewFilePickerWithResultList() {
    FilePickerWithResultList(emptyList())
}
