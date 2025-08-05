package com.nareshchocha.filepicker.components

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter

@Composable
fun FilePickerWithResultList(pickedFiles: List<PickedFile>) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(pickedFiles) { file ->
            when (file.type) {
                "image" ->
                    Image(
                        painter = rememberAsyncImagePainter(file.uri),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )

                "video" -> {
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
                            text = "Video: ${file.uri}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                else -> {
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
            }
        }
    }
}

data class PickedFile(
    val uri: Uri,
    val type: String, // "image", "video", "other"
    val filePath: String? = null
)

@Preview
@Composable
fun PreviewFilePickerWithResultList() {
    FilePickerWithResultList(emptyList())
}
