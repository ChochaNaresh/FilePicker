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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
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
            StartMediaFilePicker()
        }
    }

    @Composable
    fun StartMediaFilePicker() {
        var showAskDialog by remember { mutableStateOf(false) }
        var showGotoSettingDialog by remember { mutableStateOf(false) }
        var permissionRequested by remember { mutableStateOf(false) }

        val selectFileLauncher = rememberSelectFileLauncher()
        val permissionLauncher = rememberPermissionLauncher(
            onGranted = { launchFilePicker(this@MediaFilePickerActivity, selectFileLauncher) },
            onShowRationale = { showAskDialog = true },
            onDenied = { showGotoSettingDialog = true }
        )
        val settingLauncher = rememberSettingsLauncher(
            onGranted = { launchFilePicker(this@MediaFilePickerActivity, selectFileLauncher) },
            onDenied = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            }
        )

        // Initial permission check
        CheckInitialPermissions(
            permissionRequested = permissionRequested,
            onPermissionRequested = { permissionRequested = true },
            onCheckPermission = {
                checkPermissionFlow(
                    this@MediaFilePickerActivity,
                    permissionLauncher
                )
            },
            onLaunchPicker = { launchFilePicker(this@MediaFilePickerActivity, selectFileLauncher) }
        )

        // Show permission dialogs if needed
        DisplayPermissionDialogs(
            showAskDialog = showAskDialog,
            showGotoSettingDialog = showGotoSettingDialog,
            onAskConfirm = {
                showAskDialog = false
                permissionLauncher.launch(getPermission(mPickMediaConfig!!))
            },
            onSettingsConfirm = {
                showGotoSettingDialog = false
                settingLauncher.launch(getSettingIntent())
            }
        )
    }

    @Composable
    private fun rememberSelectFileLauncher(): ActivityResultLauncher<Intent> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleFilePickerResult(result, this@MediaFilePickerActivity)
        }
    }

    @Composable
    private fun rememberPermissionLauncher(
        onGranted: () -> Unit,
        onShowRationale: () -> Unit,
        onDenied: () -> Unit
    ): ActivityResultLauncher<String> {
        return rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onGranted()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MediaFilePickerActivity,
                    getPermission(mPickMediaConfig!!)
                )
            ) {
                onShowRationale()
            } else {
                onDenied()
            }
        }
    }

    @Composable
    private fun rememberSettingsLauncher(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ): ActivityResultLauncher<Intent> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (mPickMediaConfig != null) {
                if (ActivityCompat.checkSelfPermission(
                        this@MediaFilePickerActivity,
                        getPermission(mPickMediaConfig!!)
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    onGranted()
                } else {
                    onDenied()
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
    }

    @Composable
    private fun DisplayPermissionDialogs(
        showAskDialog: Boolean,
        showGotoSettingDialog: Boolean,
        onAskConfirm: () -> Unit,
        onSettingsConfirm: () -> Unit
    ) {
        if (showAskDialog) {
            ShowAskPermissionDialog(
                config = mPickMediaConfig,
                onConfirm = onAskConfirm,
                onDismiss = {
                    setCanceledResult(getString(R.string.err_permission_result))
                    finish()
                }
            )
        }

        if (showGotoSettingDialog) {
            ShowSettingsDialog(
                config = mPickMediaConfig,
                onConfirm = onSettingsConfirm,
                onDismiss = {
                    setCanceledResult(getString(R.string.err_permission_result))
                    finish()
                }
            )
        }
    }


    private fun handleFilePickerResult(result: ActivityResult, context: Context) {
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            if (mPickMediaConfig?.allowMultiple == true && result.data?.clipData != null) {
                val uris = result.data?.getClipDataUris()
                val filePaths = uris?.getFilePathList(context)
                setSuccessResult(uris, filePath = filePaths)
            } else if (result.data?.data != null) {
                val data = result.data?.data
                val filePath = data?.let { FileUtils.getRealPath(context, it) }
                setSuccessResult(data, filePath)
            }
        } else {
            setCanceledResult("File Picker Result Error: ${result.resultCode}")
        }
        finish()
    }


    private fun launchFilePicker(
        context: Context,
        selectFileLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    ) {
        if (mPickMediaConfig != null) {
            selectFileLauncher.launch(context.getMediaIntent(mPickMediaConfig!!))
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


        fun getInstance(mContext: Context, mPickMediaConfig: PickMediaConfig?): Intent {
            val filePickerIntent = Intent(mContext, MediaFilePickerActivity::class.java)
            mPickMediaConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICK_MEDIA, it)
            }
            return filePickerIntent
        }
    }
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

@Composable
private fun CheckInitialPermissions(
    permissionRequested: Boolean,
    onPermissionRequested: () -> Unit,
    onCheckPermission: () -> Unit,
    onLaunchPicker: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (!permissionRequested) {
            onPermissionRequested()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                onCheckPermission()
            } else {
                onLaunchPicker()
            }
        }
    }
}

@Composable
private fun ShowAskPermissionDialog(
    config: PickMediaConfig?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppAlertDialog(
        title = config?.askPermissionTitle ?: stringResource(R.string.err_permission_denied),
        message = config?.askPermissionMessage ?: stringResource(
            R.string.err_write_storage_permission,
            getPermission(config ?: PickMediaConfig()).split(".").lastOrNull() ?: ""
        ),
        confirmText = stringResource(android.R.string.ok),
        dismissText = stringResource(android.R.string.cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun ShowSettingsDialog(
    config: PickMediaConfig?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppAlertDialog(
        title = config?.settingPermissionTitle ?: stringResource(R.string.err_permission_denied),
        message = config?.settingPermissionMessage ?: stringResource(
            R.string.err_write_storage_setting,
            getPermission(config ?: PickMediaConfig()).split(".").lastOrNull() ?: ""
        ),
        confirmText = stringResource(R.string.str_go_to_setting),
        dismissText = stringResource(android.R.string.cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
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
