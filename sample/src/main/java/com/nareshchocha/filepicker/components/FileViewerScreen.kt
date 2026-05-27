package com.nareshchocha.filepicker.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nareshchocha.filepicker.R
import com.nareshchocha.filepicker.components.FileType
import java.io.File
import java.io.IOException

private const val JPEG_QUALITY = 90
private const val FILE_VIEWER_TAG = "FileViewerScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerDialog(
    file: PickedFile,
    onDismiss: () -> Unit
) {
    val url = file.filePath?.let { "file://$it" } ?: file.uri.toString()
    val fileType =
        when (file.type) {
            "image" -> FileType.IMAGE
            "video" -> FileType.VIDEO
            else -> FileType.OTHER
        }
    val title =
        file.filePath?.substringAfterLast("/")
            ?: file.uri.lastPathSegment
            ?: stringResource(R.string.label_file)
    var isLoading by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = { ViewerTopBar(title = title, onBack = onDismiss) },
            containerColor = Color.Black
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                FileWebView(
                    url = url,
                    fileType = fileType,
                    modifier = Modifier.fillMaxSize(),
                    onLoadingStart = { isLoading = true },
                    onLoadingEnd = { isLoading = false },
                    onLoadFailed = { isLoading = false }
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    title: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color.White
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
    )
}

@Composable
fun FileWebView(
    url: String,
    fileType: FileType,
    modifier: Modifier = Modifier,
    onLoadingStart: (() -> Unit)? = null,
    onLoadingEnd: (() -> Unit)? = null,
    onLoadFailed: (() -> Unit)? = null
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.builtInZoomControls = true
                settings.useWideViewPort = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mediaPlaybackRequiresUserGesture = false

                webViewClient =
                    object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?
                        ) {
                            super.onPageStarted(view, url, favicon)
                            onLoadingStart?.invoke()
                        }

                        override fun onPageFinished(
                            view: WebView?,
                            url: String?
                        ) {
                            super.onPageFinished(view, url)
                            onLoadingEnd?.invoke()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (fileType != FileType.VIDEO) {
                                onLoadFailed?.invoke()
                            }
                        }
                    }

                val isHeic = url.endsWith(".heic", ignoreCase = true) && !url.startsWith("http")
                val fileToLoad =
                    if (isHeic) {
                        val rawPath = url.removePrefix("file://")
                        val jpegPath = rawPath.replace(".heic", ".jpg", ignoreCase = true)
                        convertHeicToJpeg(rawPath, jpegPath)?.let { "file://$it" } ?: url
                    } else {
                        url
                    }

                if (fileType == FileType.VIDEO) {
                    loadDataWithBaseURL(
                        null,
                        """
                        <html><body style="margin:0;padding:0;background:#000;">
                        <video style="width:100%;height:100vh;" controls autoplay playsinline>
                          <source src="$fileToLoad">
                        </video></body></html>
                        """.trimIndent(),
                        "text/html",
                        "utf-8",
                        null
                    )
                } else {
                    loadUrl(fileToLoad)
                }
            }
        },
        modifier = modifier
    )
}

private fun convertHeicToJpeg(
    heicPath: String,
    jpegPath: String
): String? {
    val bitmap = BitmapFactory.decodeFile(heicPath) ?: return null
    return try {
        File(jpegPath).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
        }
        jpegPath
    } catch (e: IOException) {
        Log.w(FILE_VIEWER_TAG, "HEIC to JPEG conversion failed: $heicPath", e)
        null
    }
}
