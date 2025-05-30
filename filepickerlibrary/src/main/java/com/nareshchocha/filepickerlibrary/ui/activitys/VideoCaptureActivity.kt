package com.nareshchocha.filepickerlibrary.ui.activitys

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const.DefaultPaths.defaultFolder
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.getVideoCaptureIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
internal class VideoCaptureActivity : ComponentActivity() {
    private var videoFileUri: Uri? = null
    private var videoFile: File? = null

    private val mVideoCaptureConfig: VideoCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.VIDEO_CAPTURE,
                VideoCaptureConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.VIDEO_CAPTURE) as VideoCaptureConfig?
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StaringVideoCapture()
        }
    }

    @Composable
    fun StaringVideoCapture() {
        val context = LocalContext.current
        var showAskDialog by remember { mutableStateOf(false) }
        var showGotoSettingDialog by remember { mutableStateOf(false) }
        var permissionRequested by remember { mutableStateOf(false) }

        val videoCaptureLauncher = rememberVideoCaptureResultLauncher()
        val permissionLauncher = rememberPermissionResultLauncher { allGranted, isShowRationale ->
            when {
                allGranted -> launchCamera(context, videoCaptureLauncher)
                isShowRationale -> showAskDialog = true
                else -> showGotoSettingDialog = true
            }
        }
        val settingLauncher = rememberSettingResultLauncher(
            onGranted = { launchCamera(context, videoCaptureLauncher) },
            onDenied = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            }
        )
        // Initial permission check
        CheckInitialPermissions(
            permissionRequested = permissionRequested,
            onPermissionRequested = { permissionRequested = true },
            onRequestPermission = { permissionLauncher.launch(getPermissionsList(context).toTypedArray()) },
            onLaunchCamera = { launchCamera(context, videoCaptureLauncher) }
        )

        // Show dialogs if needed
        if (showAskDialog) {
            ShowAskPermissionDialog(
                context = context,
                onConfirm = {
                    showAskDialog = false
                    permissionLauncher.launch(getPermissionsList(context).toTypedArray())
                }
            )
        }

        if (showGotoSettingDialog) {
            ShowSettingsDialog(
                context = context,
                onConfirm = {
                    showGotoSettingDialog = false
                    settingLauncher.launch(getSettingIntent())
                }
            )
        }
    }

    @Composable
    private fun rememberVideoCaptureResultLauncher(): ActivityResultLauncher<Intent> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.tag(Const.LogTag.FILE_RESULT)
                    .v("File Uri ::: ${videoFileUri?.toString()}")
                Timber.tag(Const.LogTag.FILE_RESULT)
                    .v("filePath ::: ${videoFile?.absoluteFile}")
                setSuccessResult(videoFileUri, videoFile?.absolutePath, true)
            } else {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .v(getString(R.string.err_capture_error, "videoCapture"))
                setCanceledResult(getString(R.string.err_capture_error, "videoCapture"))
            }
            finish()
        }
    }

    @Composable
    private fun rememberPermissionResultLauncher(
        onResult: (allGranted: Boolean, isShowRationale: Boolean) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        val context = LocalContext.current
        return rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val allGranted = result.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission.key,
                ) == PackageManager.PERMISSION_GRANTED
            }
            val isShowRationale = result.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    permission.key,
                )
            }
            onResult(allGranted, isShowRationale)
        }
    }

    @Composable
    private fun rememberSettingResultLauncher(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ): ActivityResultLauncher<Intent> {
        val context = LocalContext.current
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val allGranted = getPermissionsList(context).all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission,
                ) == PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) onGranted() else onDenied()
        }
    }

    @Composable
    private fun CheckInitialPermissions(
        permissionRequested: Boolean,
        onPermissionRequested: () -> Unit,
        onRequestPermission: () -> Unit,
        onLaunchCamera: () -> Unit
    ) {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            if (!permissionRequested) {
                onPermissionRequested()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q || getPermissionsList(context).isNotEmpty()) {
                        onRequestPermission()
                    } else {
                        onLaunchCamera()
                    }
                } else {
                    onLaunchCamera()
                }
            }
        }
    }

    @Composable
    private fun ShowAskPermissionDialog(
        context: Context,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            },
            title = {
                Text(
                    mVideoCaptureConfig?.askPermissionTitle
                        ?: getString(R.string.err_permission_denied)
                )
            },
            text = {
                Text(
                    mVideoCaptureConfig?.askPermissionMessage ?: getString(
                        R.string.err_write_storage_permission,
                        getPermissionsListString(context),
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(getString(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    setCanceledResult(getString(R.string.err_permission_result))
                    finish()
                }) {
                    Text(getString(android.R.string.cancel))
                }
            }
        )
    }

    @Composable
    private fun ShowSettingsDialog(
        context: Context,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            },
            title = {
                Text(
                    mVideoCaptureConfig?.settingPermissionTitle
                        ?: getString(R.string.err_permission_denied)
                )
            },
            text = {
                Text(
                    mVideoCaptureConfig?.settingPermissionMessage ?: getString(
                        R.string.err_write_storage_setting,
                        getPermissionsListString(context),
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(getString(R.string.str_go_to_setting))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    setCanceledResult(getString(R.string.err_permission_result))
                    finish()
                }) {
                    Text(getString(android.R.string.cancel))
                }
            }
        )
    }


    private fun launchCamera(
        context: Context,
        videoCaptureLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    ) {
        videoFileUri = if (mVideoCaptureConfig != null) {
            videoFile = createMediaFileFolder(
                folderFile = mVideoCaptureConfig!!.mFolder ?: context.defaultFolder(),
                fileName = mVideoCaptureConfig!!.fileName ?: Const.DefaultPaths.defaultVideoFile(),
            )
            createFileGetUri(videoFile!!)
        } else {
            videoFile = createMediaFileFolder(
                folderFile = context.defaultFolder(),
                fileName = Const.DefaultPaths.defaultVideoFile(),
            )
            createFileGetUri(videoFile!!)
        }
        videoFileUri?.let {
            videoCaptureLauncher.launch(
                getVideoCaptureIntent(
                    it,
                    maxSeconds = mVideoCaptureConfig?.maxSeconds,
                    maxSizeLimit = mVideoCaptureConfig?.maxSizeLimit,
                    isHighQuality = mVideoCaptureConfig?.isHighQuality,
                ),
            )
        }
    }

    private fun getPermissionsListString(context: Context): String {
        val listString = getPermissionsList(context).map {
            it.split(".").lastOrNull() ?: ""
        }.toString()
        return listString.substring(1, listString.length - 1).replace(",", " and ")
    }

    companion object {
        @Keep
        private fun getPermissionsList(context: Context) = ArrayList<String>().also {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                it.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            val permissions: Array<String>? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                    ).requestedPermissions
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_PERMISSIONS
                    ).requestedPermissions
                }
            if (permissions?.contains(Manifest.permission.CAMERA) == true) {
                it.add(Manifest.permission.CAMERA)
            }
        }

        @Keep
        fun getInstance(mContext: Context, mVideoCaptureConfig: VideoCaptureConfig?): Intent {
            val filePickerIntent = Intent(mContext, VideoCaptureActivity::class.java)
            mVideoCaptureConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.VIDEO_CAPTURE, it)
            }
            filePickerIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            return filePickerIntent
        }
    }
}
