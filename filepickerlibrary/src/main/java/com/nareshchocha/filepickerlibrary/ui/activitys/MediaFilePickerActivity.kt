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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.getMediaIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.getRequestedPermissions
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult
import timber.log.Timber

internal class MediaFilePickerActivity : ComponentActivity() {

    private val mPickMediaConfig: PickMediaConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICK_MEDIA,
                PickMediaConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.PICK_MEDIA) as PickMediaConfig?
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var showAskDialog by remember { mutableStateOf(false) }
            var showGotoSettingDialog by remember { mutableStateOf(false) }
            var permissionRequested by remember { mutableStateOf(false) }

            val selectFileLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        if (mPickMediaConfig?.allowMultiple == true && result.data?.clipData != null) {
                            val uris = result.data?.getClipDataUris()
                            Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: $uris")
                            val filePaths = uris?.getFilePathList(context)
                            Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePaths")
                            setSuccessResult(uris, filePath = filePaths)
                        } else if (result.data?.data != null) {
                            val data = result.data?.data
                            Timber.tag(Const.LogTag.FILE_RESULT)
                                .v("File Uri ::: ${data?.toString()}")
                            val filePath = data?.let { FileUtils.getRealPath(context, it) }
                            Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePath")
                            setSuccessResult(data, filePath)
                        }
                    } else {
                        Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                            .v(getString(R.string.err_media_pick_error))
                        setCanceledResult(getString(R.string.err_media_pick_error))
                    }
                    finish()
                }

            val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (granted) {
                        launchFilePicker(context, selectFileLauncher)
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            getPermission(mPickMediaConfig!!)
                        )
                    ) {
                        showAskDialog = true
                    } else {
                        showGotoSettingDialog = true
                    }
                }

            val settingLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (mPickMediaConfig != null) {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                getPermission(mPickMediaConfig!!)
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            launchFilePicker(context, selectFileLauncher)
                        } else {
                            setCanceledResult(getString(R.string.err_permission_result))
                            finish()
                        }
                    } else {
                        setCanceledResult(
                            getString(
                                R.string.err_config_null,
                                this::mPickMediaConfig::class.java.name,
                            ),
                        )
                        finish()
                    }
                }

            // Initial permission check
            LaunchedEffect(Unit) {
                if (!permissionRequested) {
                    permissionRequested = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        launchFilePicker(context, selectFileLauncher)
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
                            mPickMediaConfig?.askPermissionTitle
                                ?: getString(R.string.err_permission_denied)
                        )
                    },
                    text = {
                        Text(
                            mPickMediaConfig?.askPermissionMessage ?: getString(
                                R.string.err_write_storage_permission,
                                getPermission(mPickMediaConfig!!).split(".").lastOrNull() ?: "",
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showAskDialog = false
                            permissionLauncher.launch(getPermission(mPickMediaConfig!!))
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
                            mPickMediaConfig?.settingPermissionTitle
                                ?: getString(R.string.err_permission_denied)
                        )
                    },
                    text = {
                        Text(
                            mPickMediaConfig?.settingPermissionMessage ?: getString(
                                R.string.err_write_storage_setting,
                                getPermission(mPickMediaConfig!!).split(".").lastOrNull() ?: "",
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

    private fun launchFilePicker(
        context: Context,
        selectFileLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    ) {
        if (mPickMediaConfig != null) {
            selectFileLauncher.launch(getMediaIntent(mPickMediaConfig!!))
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mPickMediaConfig::class.java.name,
                ),
            )
            finish()
        }
    }

    private fun checkPermissionFlow(
        context: Context,
        permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    ) {
        if (mPickMediaConfig != null) {
            val list = getPermissionManifestCheck(mPickMediaConfig!!, context)
            if (list.isEmpty()) {
                setCanceledResult(getString(R.string.permission_not_found))
                finish()
            } else {
                permissionLauncher.launch(getPermission(mPickMediaConfig!!))
            }
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mPickMediaConfig::class.java.name,
                ),
            )
            finish()
        }
    }

    private fun Intent.getClipDataUris(): ArrayList<Uri> {
        val resultSet = LinkedHashSet<Uri>()
        data?.let { data ->
            resultSet.add(data)
        }
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

    private fun List<Uri>.getFilePathList(context: Context): ArrayList<String> {
        val filePathList = ArrayList<String>()
        forEach { uri ->
            FileUtils.getRealPath(context, uri)?.also { filePath ->
                filePathList.add(filePath)
            }
        }
        return filePathList
    }

    companion object {

        private fun getPermissionManifestCheck(
            mPickMediaConfig: PickMediaConfig,
            context: Context
        ) = ArrayList<String>().also {
            val permissions = context.getRequestedPermissions()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (mPickMediaConfig.mPickMediaType == PickMediaType.ImageOnly) {
                    if (permissions?.contains(Manifest.permission.READ_MEDIA_IMAGES) == true) {
                        it.add(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    if (permissions?.contains(Manifest.permission.READ_MEDIA_VIDEO) == true) {
                        it.add(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                }
            } else {
                if (permissions?.contains(Manifest.permission.READ_EXTERNAL_STORAGE) == true) {
                    it.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        private fun getPermission(mPickMediaConfig: PickMediaConfig): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (mPickMediaConfig.mPickMediaType == PickMediaType.ImageOnly) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_MEDIA_VIDEO
                }
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        fun getInstance(mContext: Context, mPickMediaConfig: PickMediaConfig?): Intent {
            val filePickerIntent = Intent(mContext, MediaFilePickerActivity::class.java)
            mPickMediaConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICK_MEDIA, it)
            }
            return filePickerIntent
        }
    }
}