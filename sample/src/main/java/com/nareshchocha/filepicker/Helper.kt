package com.nareshchocha.filepicker

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nareshchocha.filepickerlibrary.models.Orientation
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.PopUpType
import com.nareshchocha.filepickerlibrary.ui.FilePicker

@Composable
fun AllOptionsButton(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(
        onClick = {
            launcher.launch(
                FilePicker
                    .Builder(context)
                    .setPopUpConfig(
                        PopUpConfig(
                            mPopUpType = PopUpType.BOTTOM_SHEET,
                            mOrientation = Orientation.VERTICAL,
                            chooserTitle = "Choose Profile"
                        )
                    ).addPickDocumentFile()
                    .addImageCapture()
                    .addVideoCapture()
                    .addPickMedia()
                    .build()
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("All Options") }
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
