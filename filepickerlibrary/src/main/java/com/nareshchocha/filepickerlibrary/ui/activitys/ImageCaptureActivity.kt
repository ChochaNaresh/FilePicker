package com.nareshchocha.filepickerlibrary.ui.activitys

import android.Manifest
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppAlertDialog
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const.DefaultPaths.defaultFolder
import com.nareshchocha.filepickerlibrary.utilities.extensions.getImageCaptureIntent
import com.nareshchocha.filepickerlibrary.utilities.extensions.getRequestedPermissions
import com.nareshchocha.filepickerlibrary.utilities.extensions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extensions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extensions.setSuccessResult
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
internal class ImageCaptureActivity : ComponentActivity() {
    private var imageFileUri: Uri? = null
    private var imageFile: File? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetUI()
        }
    }

    @Composable
    fun SetUI() {
        val context = LocalContext.current
        var showAskDialog by remember { mutableStateOf(false) }
        var showGotoSettingDialog by remember { mutableStateOf(false) }
        var permissionRequested by remember { mutableStateOf(false) }

        val imageCaptureLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                handleImageCaptureResult(result.resultCode)
            }

        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                handlePermissionResult(
                    result,
                    context,
                    imageCaptureLauncher
                ) { askDialog, settingDialog ->
                    showAskDialog = askDialog
                    showGotoSettingDialog = settingDialog
                }
            }

        val settingLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                checkPermissionsAndLaunchCamera(context, imageCaptureLauncher)
            }

        // Initial permission check
        LaunchedEffect(Unit) {
            if (!permissionRequested) {
                permissionRequested = true
                checkAndRequestPermissions(
                    context,
                    permissionLauncher = {
                        permissionLauncher.launch(getPermissionsList(context).toTypedArray())
                    },
                    launchCamera = {
                        launchCamera(context, imageCaptureLauncher)
                    }
                )
            }
        }
        // Ask Permission Dialog
        if (showAskDialog) {
            PermissionDialog(context, onConfirm = {
                showAskDialog = false
                permissionLauncher.launch(getPermissionsList(context).toTypedArray())
            }, onDismiss = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            })
        }

        // Go to Setting Dialog
        if (showGotoSettingDialog) {
            SettingDialog(context, onConfirm = {
                showGotoSettingDialog = false
                settingLauncher.launch(getSettingIntent())
            }, onDismiss = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            })
        }
    }

    private fun checkAndRequestPermissions(
        context: Context,
        permissionLauncher: (Array<String>) -> Unit,
        launchCamera: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsList = getPermissionsList(context)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q || permissionsList.isNotEmpty()) {
                permissionLauncher(permissionsList.toTypedArray())
            } else {
                launchCamera()
            }
        } else {
            launchCamera()
        }
    }

    private fun checkPermissionsAndLaunchCamera(
        context: Context,
        imageCaptureLauncher: ActivityResultLauncher<Intent>
    ) {
        val allGranted =
            getPermissionsList(context).all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }

        if (allGranted) {
            launchCamera(context, imageCaptureLauncher)
        } else {
            setCanceledResult(getString(R.string.err_permission_result))
            finish()
        }
    }

    private fun handlePermissionResult(
        result: Map<String, Boolean>,
        context: Context,
        imageCaptureLauncher: ActivityResultLauncher<Intent>,
        onShowDialog: (askDialog: Boolean, settingDialog: Boolean) -> Unit
    ) {
        val allGranted =
            result.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission.key
                ) == PackageManager.PERMISSION_GRANTED
            }

        val isShowRationale =
            result.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission.key
                )
            }

        if (allGranted) {
            launchCamera(context, imageCaptureLauncher)
        } else if (isShowRationale) {
            onShowDialog(true, false)
        } else {
            onShowDialog(false, true)
        }
    }

    private fun handleImageCaptureResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            setSuccessResult(imageFileUri, imageFile?.absolutePath, true)
        } else {
            setCanceledResult("File Picker Result Error: $resultCode")
        }
        finish()
    }

    @Composable
    private fun PermissionDialog(
        context: Context,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AppAlertDialog(
            onDismissRequest = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            },
            title =
                mImageCaptureConfig?.askPermissionTitle
                    ?: stringResource(R.string.err_permission_denied),
            message =
                mImageCaptureConfig?.askPermissionMessage ?: stringResource(
                    R.string.err_write_storage_permission,
                    getPermissionsListString(context)
                ),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    @Composable
    private fun SettingDialog(
        context: Context,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AppAlertDialog(
            onDismissRequest = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            },
            title =
                mImageCaptureConfig?.settingPermissionTitle
                    ?: stringResource(R.string.err_permission_denied),
            message =
                mImageCaptureConfig?.settingPermissionMessage ?: getString(
                    R.string.err_write_storage_setting,
                    getPermissionsListString(context)
                ),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    private fun launchCamera(
        context: Context,
        imageCaptureLauncher: ActivityResultLauncher<Intent>
    ) {
        imageFileUri =
            if (mImageCaptureConfig != null) {
                imageFile =
                    createMediaFileFolder(
                        folderFile = mImageCaptureConfig!!.mFolder ?: context.defaultFolder(),
                        fileName = mImageCaptureConfig!!.fileName ?: Const.DefaultPaths.defaultImageFile()
                    )
                context.createFileGetUri(imageFile!!)
            } else {
                imageFile =
                    createMediaFileFolder(
                        folderFile = context.defaultFolder(),
                        fileName = Const.DefaultPaths.defaultImageFile()
                    )
                context.createFileGetUri(imageFile!!)
            }
        imageFileUri?.let {
            imageCaptureLauncher.launch(
                context.getImageCaptureIntent(
                    it,
                    mImageCaptureConfig?.isUseRearCamera ?: true
                )
            )
        }
    }

    private fun getPermissionsListString(context: Context): String {
        val listString =
            getPermissionsList(context)
                .map {
                    it.split(".").lastOrNull() ?: ""
                }.toString()
        return listString.substring(1, listString.length - 1).replace(",", " and ")
    }

    companion object {
        @Keep
        private fun getPermissionsList(context: Context) =
            ArrayList<String>().also {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    it.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                val permissions = context.getRequestedPermissions()
                if (permissions?.contains(Manifest.permission.CAMERA) == true) {
                    it.add(Manifest.permission.CAMERA)
                }
            }

        @Keep
        fun getInstance(
            mContext: Context,
            mImageCaptureConfig: ImageCaptureConfig?
        ): Intent {
            val filePickerIntent = Intent(mContext, ImageCaptureActivity::class.java)
            mImageCaptureConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.IMAGE_CAPTURE, it)
            }
            return filePickerIntent
        }
    }
}
