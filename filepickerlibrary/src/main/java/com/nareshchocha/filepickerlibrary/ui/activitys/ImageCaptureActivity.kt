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
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
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
import com.nareshchocha.filepickerlibrary.utilities.extensions.getImageCaptureIntent
import com.nareshchocha.filepickerlibrary.utilities.extensions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extensions.setSuccessResult
import java.io.File

internal class ImageCaptureActivity : ComponentActivity() {
    private val mImageCaptureConfig: ImageCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.IMAGE_CAPTURE,
                ImageCaptureConfig::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.IMAGE_CAPTURE) as ImageCaptureConfig?
        }
    }
    private var imageFile: File? = null
    private val imageFileUri: Uri? by lazy {
        imageFile =
            createMediaFileFolder(
                folderFile = mImageCaptureConfig!!.mFolder ?: defaultFolder(),
                fileName =
                    mImageCaptureConfig!!.fileName
                        ?: Const.DefaultPaths.defaultImageFile()
            )
        createFileGetUri(imageFile)
    }

    val imageCaptureLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                setSuccessResult(imageFileUri, imageFile?.absolutePath, true)
            } else {
                imageFile?.delete()
                setCanceledResult("File Picker Result Error: ${result.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mImageCaptureConfig == null) {
            imageFile?.delete()
            setCanceledResult(getString(R.string.image_capture_config_null_error))
            return
        } else {
            setContent {
                StartImageCapture()
            }
        }
    }

    @Composable
    fun StartImageCapture() {
        val activity = LocalContext.current.getActivityOrNull() ?: this
        var showRationaleDialog by remember { mutableStateOf(false) }
        var showSettingDialog by remember { mutableStateOf(false) }
        val mMultiplePermissionManager =
            MultiplePermissionManager(
                activity = activity,
                permissions = PermissionLists.imageCapturePermissions(activity),
                onPermissionsMissing = {
                    imageFile?.delete()
                    activity.setCanceledResult(
                        getString(
                            R.string.err_permission_missing,
                            it.asString()
                        )
                    )
                },
                onMultiplePermissionsGranted = {
                    imageCapture()
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

    fun imageCapture() {
        if (imageFileUri != null) {
            imageCaptureLauncher.launch(
                getImageCaptureIntent(
                    imageFileUri!!,
                    mImageCaptureConfig?.isUseRearCamera == true
                )
            )
        } else {
            imageFile?.delete()
            setCanceledResult(getString(R.string.err_file_creation_failed))
        }
    }

    fun setPermissionDeniedResult(permissions: List<String>) {
        imageFile?.delete()
        setCanceledResult(
            getString(
                R.string.permission_denied,
                permissions.asString()
            )
        )
    }

    @Composable
    private fun ShowSettingDialog(
        permissions: List<String>,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AppSettingDialog(
            permissions = permissions,
            title = mImageCaptureConfig?.settingPermissionTitle,
            message = mImageCaptureConfig?.settingPermissionMessage,
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
                mImageCaptureConfig?.askPermissionTitle
                    ?: stringResource(R.string.err_permissions_rationale_title),
            message =
                mImageCaptureConfig?.askPermissionMessage ?: stringResource(
                    R.string.err_capture_permissions_rationale_message
                ),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    companion object {
        fun getInstance(
            context: Context,
            mImageCaptureConfig: ImageCaptureConfig?
        ): Intent =
            Intent(context, ImageCaptureActivity::class.java).apply {
                mImageCaptureConfig?.let { putExtra(Const.BundleInternalExtras.IMAGE_CAPTURE, it) }
            }
    }
}
