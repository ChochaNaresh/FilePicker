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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const.DefaultPaths.defaultFolder
import com.nareshchocha.filepickerlibrary.utilities.extentions.getImageCaptureIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.getRequestedPermissions
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
internal class ImageCaptureActivity : ComponentActivity() {
    private var imageFileUri: Uri? = null
    private var imageFile: File? = null

    private val mImageCaptureConfig: ImageCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.IMAGE_CAPTURE,
                ImageCaptureConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.IMAGE_CAPTURE) as ImageCaptureConfig?
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var showAskDialog by remember { mutableStateOf(false) }
            var showGotoSettingDialog by remember { mutableStateOf(false) }
            var permissionRequested by remember { mutableStateOf(false) }

            val imageCaptureLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        Timber.tag(Const.LogTag.FILE_RESULT)
                            .v("File Uri ::: ${imageFileUri?.toString()}")
                        Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: ${imageFile?.absoluteFile}")
                        Timber.tag(Const.LogTag.FILE_RESULT).v("file read:: ${imageFile?.canRead()}")
                        setSuccessResult(imageFileUri, imageFile?.absolutePath, true)
                    } else {
                        Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                            .v(getString(R.string.err_capture_error, "imageCapture"))
                        setCanceledResult(getString(R.string.err_capture_error, "imageCapture"))
                    }
                    finish()
                }

            val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                    val allGranted = result.all { permission ->
                        ContextCompat.checkSelfPermission(
                            context,
                            permission.key,
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    val isShowRationale = result.any { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permission.key,
                        )
                    }
                    if (allGranted) {
                        launchCamera(context, imageCaptureLauncher)
                    } else if (isShowRationale) {
                        showAskDialog = true
                    } else {
                        showGotoSettingDialog = true
                    }
                }

            val settingLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    val allGranted = getPermissionsList(context).all { permission ->
                        ContextCompat.checkSelfPermission(
                            context,
                            permission,
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                    if (allGranted) {
                        launchCamera(context, imageCaptureLauncher)
                    } else {
                        setCanceledResult(getString(R.string.err_permission_result))
                        finish()
                    }
                }

            // Initial permission check
            LaunchedEffect(Unit) {
                if (!permissionRequested) {
                    permissionRequested = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q || getPermissionsList(context).isNotEmpty()) {
                            permissionLauncher.launch(getPermissionsList(context).toTypedArray())
                        } else {
                            launchCamera(context, imageCaptureLauncher)
                        }
                    } else {
                        launchCamera(context, imageCaptureLauncher)
                    }
                }
            }

            // Ask Permission Dialog
            if (showAskDialog) {
                AlertDialog(
                    onDismissRequest = {
                        setCanceledResult(getString(R.string.err_permission_result))
                        finish()
                    },
                    title = {
                        Text(
                            mImageCaptureConfig?.askPermissionTitle
                                ?: getString(R.string.err_permission_denied)
                        )
                    },
                    text = {
                        Text(
                            mImageCaptureConfig?.askPermissionMessage ?: getString(
                                R.string.err_write_storage_permission,
                                getPermissionsListString(context),
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showAskDialog = false
                            permissionLauncher.launch(getPermissionsList(context).toTypedArray())
                        }) { Text(getString(android.R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            setCanceledResult(getString(R.string.err_permission_result))
                            finish()
                        }) { Text(getString(android.R.string.cancel)) }
                    }
                )
            }

            // Go to Setting Dialog
            if (showGotoSettingDialog) {
                AlertDialog(
                    onDismissRequest = {
                        setCanceledResult(getString(R.string.err_permission_result))
                        finish()
                    },
                    title = {
                        Text(
                            mImageCaptureConfig?.settingPermissionTitle
                                ?: getString(R.string.err_permission_denied)
                        )
                    },
                    text = {
                        Text(
                            mImageCaptureConfig?.settingPermissionMessage ?: getString(
                                R.string.err_write_storage_setting,
                                getPermissionsListString(context),
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showGotoSettingDialog = false
                            settingLauncher.launch(getSettingIntent())
                        }) { Text(getString(R.string.str_go_to_setting)) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            setCanceledResult(getString(R.string.err_permission_result))
                            finish()
                        }) { Text(getString(android.R.string.cancel)) }
                    }
                )
            }
        }
    }

    private fun launchCamera(
        context: Context,
        imageCaptureLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    ) {
        imageFileUri = if (mImageCaptureConfig != null) {
            imageFile = createMediaFileFolder(
                folderFile = mImageCaptureConfig!!.mFolder ?: defaultFolder(),
                fileName = mImageCaptureConfig!!.fileName ?: Const.DefaultPaths.defaultImageFile(),
            )
            createFileGetUri(imageFile!!)
        } else {
            imageFile = createMediaFileFolder(
                folderFile = defaultFolder(),
                fileName = Const.DefaultPaths.defaultImageFile(),
            )
            createFileGetUri(imageFile!!)
        }
        imageFileUri?.let {
            imageCaptureLauncher.launch(
                getImageCaptureIntent(
                    it,
                    mImageCaptureConfig?.isUseRearCamera ?: true
                )
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
            val permissions = context.getRequestedPermissions()
            if (permissions?.contains(Manifest.permission.CAMERA) == true) {
                it.add(Manifest.permission.CAMERA)
            }
        }

        @Keep
        fun getInstance(mContext: Context, mImageCaptureConfig: ImageCaptureConfig?): Intent {
            val filePickerIntent = Intent(mContext, ImageCaptureActivity::class.java)
            mImageCaptureConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.IMAGE_CAPTURE, it)
            }
            return filePickerIntent
        }
    }
}