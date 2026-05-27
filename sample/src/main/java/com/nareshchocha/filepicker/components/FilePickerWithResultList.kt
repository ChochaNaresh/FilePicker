package com.nareshchocha.filepicker.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepicker.R
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import java.util.UUID

@Composable
fun FileItem(file: PickedFile) {
    val context = LocalContext.current
    var showViewer by remember { mutableStateOf(false) }

    val clickModifier =
        Modifier.clickable {
            if (file.type == "image" || file.type == "video") {
                showViewer = true
            } else {
                openFile(context, file.uri, file.mimeType)
            }
        }
    when (file.type) {
        "image" -> ImageItem(file = file, modifier = clickModifier)
        "video" -> VideoItem(file = file, modifier = clickModifier)
        else -> OtherFileItem(file = file, modifier = clickModifier)
    }

    if (showViewer) {
        FileViewerDialog(file = file, onDismiss = { showViewer = false })
    }
}

@Composable
private fun ImageItem(
    file: PickedFile,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
    ) {
        CoilImage(
            imageModel = { file.uri },
            imageOptions =
                ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
            modifier = Modifier.matchParentSize()
        )
        val label =
            file.filePath?.substringAfterLast("/")
                ?: file.uri.lastPathSegment
                ?: stringResource(R.string.label_image)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    ).padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun VideoItem(
    file: PickedFile,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ).padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = stringResource(R.string.cd_video),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column {
            Text(
                text =
                    file.filePath?.substringAfterLast("/")
                        ?: file.uri.lastPathSegment
                        ?: stringResource(R.string.label_video),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.filePath ?: file.uri.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OtherFileItem(
    file: PickedFile,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ).padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AttachFile,
            contentDescription = stringResource(R.string.cd_file),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column {
            Text(
                text =
                    file.filePath?.substringAfterLast("/")
                        ?: file.uri.lastPathSegment
                        ?: stringResource(R.string.label_file),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.filePath ?: file.uri.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun openFile(
    context: Context,
    uri: Uri,
    mimeType: String?
) {
    val viewIntent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    val chooser =
        Intent.createChooser(viewIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.no_app_to_open_file), Toast.LENGTH_SHORT).show()
    }
}

@Immutable
data class PickedFile(
    val uri: Uri,
    val type: String, // "image", "video", "other"
    val mimeType: String? = null,
    val filePath: String? = null,
    val uuid: String = UUID.randomUUID().toString()
)
