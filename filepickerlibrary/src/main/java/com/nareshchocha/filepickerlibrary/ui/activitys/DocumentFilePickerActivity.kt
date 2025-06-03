package com.nareshchocha.filepickerlibrary.ui.activitys

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.getDocumentFilePick
import com.nareshchocha.filepickerlibrary.utilities.extentions.getRequestedPermissions
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult

@OptIn(ExperimentalMaterial3Api::class)
internal class DocumentFilePickerActivity : ComponentActivity() {

    private val mDocumentFilePickerConfig: DocumentFilePickerConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICK_DOCUMENT,
                DocumentFilePickerConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.PICK_DOCUMENT) as DocumentFilePickerConfig?
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StatingDocument()
        }
    }

    @Composable
    fun StatingDocument() {
        var showAskDialog by remember { mutableStateOf(false) }
        var showGotoSettingDialog by remember { mutableStateOf(false) }
        var permissionRequested by remember { mutableStateOf(false) }

        val filePickerLauncher = rememberFilePickerLauncher()
        val permissionLauncher = rememberPermissionLauncher(
            onGranted = { launchFilePicker(filePickerLauncher) },
            onShowRationale = { showAskDialog = true },
            onDenied = { showGotoSettingDialog = true }
        )
        val settingLauncher = rememberSettingsLauncher(
            onGranted = { launchFilePicker(filePickerLauncher) },
            onDenied = {
                setCanceledResult(getString(R.string.err_permission_result))
                finish()
            }
        )

        // Initial permission check
        CheckInitialPermissions(
            permissionRequested = permissionRequested,
            onPermissionRequested = { permissionRequested = true },
            onCheckPermission = { checkPermissionFlow(permissionLauncher) },
            onLaunchPicker = { launchFilePicker(filePickerLauncher) }
        )

        // Permission dialogs
        if (showAskDialog) {
            ShowAskPermissionDialog(
                mDocumentFilePickerConfig = mDocumentFilePickerConfig,
                onConfirm = {
                    showAskDialog = false
                    permissionLauncher.launch(getPermission())
                },
                onDismiss = {
                    setCanceledResult(getString(R.string.err_permission_result))
                    finish()
                }
            )
        }

        if (showGotoSettingDialog) {
            ShowSettingsPermissionDialog(
                onConfirm = {
                    showGotoSettingDialog = false
                    settingLauncher.launch(getSettingIntent())
                },
                onDismiss = {
                    setCanceledResult(getString(R.string.err_permission_result))
                    finish()
                }
            )
        }
    }

    @Composable
    private fun rememberFilePickerLauncher(): ManagedActivityResultLauncher<Intent, ActivityResult> {
        return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleFilePickerResult(result)
        }
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
    private fun ShowSettingsPermissionDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    mDocumentFilePickerConfig?.settingPermissionTitle
                        ?: getString(R.string.err_permission_denied)
                )
            },
            text = {
                Text(
                    mDocumentFilePickerConfig?.settingPermissionMessage ?: getString(
                        R.string.err_write_storage_setting,
                        getPermission().split(".").lastOrNull() ?: "",
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text(getString(R.string.str_go_to_setting)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(getString(android.R.string.cancel)) }
            }
        )
    }

    private fun checkPermissionFlow(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
        if (mDocumentFilePickerConfig != null) {
            val list = getPermissionManifestCheck(this)
            if (list.isEmpty()) {
                setCanceledResult(getString(R.string.permission_not_found))
                finish()
            } else {
                permissionLauncher.launch(getPermission())
            }
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mDocumentFilePickerConfig::class.java.name
                )
            )
            finish()
        }
    }

    private fun launchFilePicker(filePickerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (mDocumentFilePickerConfig != null) {
            filePickerLauncher.launch(getDocumentFilePick(mDocumentFilePickerConfig!!))
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mDocumentFilePickerConfig::class.java.name
                )
            )
            finish()
        }
    }

    private fun handleFilePickerResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK && result.data != null) {
            if (mDocumentFilePickerConfig?.allowMultiple == true && result.data?.clipData != null) {
                val uris = result.data?.getClipDataUris()
                val filePaths = uris?.getFilePathList(this)
                setSuccessResult(uris, filePath = filePaths)
            } else if (result.data?.data != null) {
                val data = result.data?.data
                val filePath = data?.let { FileUtils.getRealPath(this, it) }
                setSuccessResult(data, filePath)
            }
        } else {
            setCanceledResult("File Picker Result Error: ${result.resultCode}")
        }
        finish()
    }

    private fun Intent.getClipDataUris(): ArrayList<Uri> {
        val resultSet = LinkedHashSet<Uri>()
        data?.let { resultSet.add(it) }
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
        private fun getPermissionManifestCheck(context: Context) = ArrayList<String>().also {
            val permissions = context.getRequestedPermissions()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (permissions?.contains(Manifest.permission.READ_MEDIA_VIDEO) == true) {
                    it.add(Manifest.permission.READ_MEDIA_VIDEO)
                }
            } else {
                if (permissions?.contains(Manifest.permission.READ_EXTERNAL_STORAGE) == true) {
                    it.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }


        @Keep
        fun getInstance(
            mContext: Context,
            mDocumentFilePickerConfig: DocumentFilePickerConfig?,
        ): Intent {
            val filePickerIntent = Intent(mContext, DocumentFilePickerActivity::class.java)
            mDocumentFilePickerConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICK_DOCUMENT, it)
            }
            return filePickerIntent
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
private fun rememberPermissionLauncher(
    onGranted: () -> Unit,
    onShowRationale: () -> Unit,
    onDenied: () -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    val activity = LocalContext.current as ComponentActivity
    return rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onGranted()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                getPermission()
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
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (ActivityCompat.checkSelfPermission(
                context,
                getPermission()
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            onDenied()
        }
    }
}

private fun getPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

@Composable
private fun ShowAskPermissionDialog(
    mDocumentFilePickerConfig: DocumentFilePickerConfig? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                mDocumentFilePickerConfig?.askPermissionTitle
                    ?: stringResource(R.string.err_permission_denied)
            )
        },
        text = {
            Text(
                mDocumentFilePickerConfig?.askPermissionMessage ?: stringResource(
                    R.string.err_write_storage_permission,
                    getPermission().split(".").lastOrNull() ?: "",
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}
