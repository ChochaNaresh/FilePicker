package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppRationaleDialog
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppSettingDialog
import com.nareshchocha.filepickerlibrary.utilities.MultiplePermissionManager
import com.nareshchocha.filepickerlibrary.utilities.PermissionLists
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const.DefaultPaths.defaultFolder
import com.nareshchocha.filepickerlibrary.utilities.extensions.asString
import com.nareshchocha.filepickerlibrary.utilities.extensions.getActivityOrNull
import com.nareshchocha.filepickerlibrary.utilities.extensions.getVideoCaptureIntent
import com.nareshchocha.filepickerlibrary.utilities.extensions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extensions.setSuccessResult
import java.io.File

internal class VideoCaptureActivity : ComponentActivity() {
    private val mVideoCaptureConfig: VideoCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.VIDEO_CAPTURE,
                VideoCaptureConfig::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.VIDEO_CAPTURE) as VideoCaptureConfig?
        }
    }
    private var videoFile: File? = null
    private val videoFileUri: Uri? by lazy {
        videoFile =
            createMediaFileFolder(
                folderFile = mVideoCaptureConfig?.mFolder ?: defaultFolder(),
                fileName = mVideoCaptureConfig?.fileName ?: Const.DefaultPaths.defaultVideoFile()
            )
        createFileGetUri(videoFile)
    }

    val videoCaptureLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                setSuccessResult(videoFileUri, videoFile?.absolutePath, true)
            } else {
                videoFile?.delete()
                setCanceledResult("File Picker Result Error: ${result.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mVideoCaptureConfig == null) {
            videoFile?.delete()
            setCanceledResult(getString(R.string.image_capture_config_null_error))
            return
        } else {
            setContent {
                StaringVideoCapture()
            }
        }
    }

    @Composable
    fun StaringVideoCapture() {
        val activity = LocalContext.current.getActivityOrNull() ?: this
        var showRationaleDialog by remember { mutableStateOf(false) }
        var showSettingDialog by remember { mutableStateOf(false) }
        val mMultiplePermissionManager =
            MultiplePermissionManager(
                activity = activity,
                permissions = PermissionLists.videoCapturePermissions(activity),
                onPermissionsMissing = {
                    videoFile?.delete()
                    activity.setCanceledResult(
                        getString(
                            R.string.err_permission_missing,
                            it.asString()
                        )
                    )
                },
                onMultiplePermissionsGranted = {
                    videoCapture()
                },
                onMultiplePermissionsDenied = {
                    showSettingDialog = true
                },
                onShowMultipleRationale = {
                    showRationaleDialog = true
                }
            )
        mMultiplePermissionManager.Check()

        if (showSettingDialog) {
            ShowSettingDialog(
                permissions = mMultiplePermissionManager.getDeniedPermissions(),
                onConfirm = {
                    showSettingDialog = false
                    mMultiplePermissionManager.openAppSettings()
                },
                onDismiss = {
                    showSettingDialog = false
                    setPermissionDeniedResult(mMultiplePermissionManager.getDeniedPermissions())
                }
            )
        }

        if (showRationaleDialog) {
            ShowRationaleDialog(
                onConfirm = {
                    showRationaleDialog = false
                    mMultiplePermissionManager.permissionsCheck()
                },
                onDismiss = {
                    showRationaleDialog = false
                    setPermissionDeniedResult(mMultiplePermissionManager.getRationalePermissions())
                }
            )
        }
    }

    @Composable
    private fun ShowSettingDialog(
        permissions: List<String>,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AppSettingDialog(
            permissions = permissions,
            title = mVideoCaptureConfig?.settingPermissionTitle,
            message = mVideoCaptureConfig?.settingPermissionMessage,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    @Composable
    private fun ShowRationaleDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AppRationaleDialog(
            title =
                mVideoCaptureConfig?.askPermissionTitle
                    ?: stringResource(R.string.err_permissions_rationale_title),
            message =
                mVideoCaptureConfig?.askPermissionMessage ?: stringResource(
                    R.string.err_capture_permissions_rationale_message
                ),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    fun videoCapture() {
        if (videoFileUri != null) {
            videoCaptureLauncher.launch(
                getVideoCaptureIntent(
                    videoFileUri!!,
                    maxSeconds = mVideoCaptureConfig?.maxSeconds,
                    maxSizeLimit = mVideoCaptureConfig?.maxSizeLimit,
                    isHighQuality = mVideoCaptureConfig?.isHighQuality
                )
            )
        } else {
            videoFile?.delete()
            setCanceledResult(getString(R.string.err_file_creation_failed))
        }
    }

    fun setPermissionDeniedResult(permissions: List<String>) {
        videoFile?.delete()
        setCanceledResult(
            getString(
                R.string.permission_denied,
                permissions.asString()
            )
        )
    }

    companion object {
        fun getInstance(
            mContext: Context,
            mVideoCaptureConfig: VideoCaptureConfig?
        ): Intent =
            Intent(mContext, VideoCaptureActivity::class.java).apply {
                mVideoCaptureConfig?.let {
                    putExtra(Const.BundleInternalExtras.VIDEO_CAPTURE, it)
                }
            }
    }
}
