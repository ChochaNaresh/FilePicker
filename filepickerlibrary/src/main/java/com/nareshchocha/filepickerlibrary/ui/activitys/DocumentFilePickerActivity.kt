package com.nareshchocha.filepickerlibrary.ui.activitys

import android.Manifest
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import timber.log.Timber

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
            var showAskDialog by remember { mutableStateOf(false) }
            var showGotoSettingDialog by remember { mutableStateOf(false) }
            var permissionRequested by remember { mutableStateOf(false) }
            val filePickerLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    handleFilePickerResult(result)
                }
            // Launchers
            val permissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (granted) {
                        launchFilePicker(filePickerLauncher)
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            getPermission()
                        )
                    ) {
                        showAskDialog = true
                    } else {
                        showGotoSettingDialog = true
                    }
                }

            val settingLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            getPermission()
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchFilePicker(filePickerLauncher)
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
                        checkPermissionFlow(permissionLauncher)
                    } else {
                        launchFilePicker(filePickerLauncher)
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
                            mDocumentFilePickerConfig?.askPermissionTitle
                                ?: getString(R.string.err_permission_denied)
                        )
                    },
                    text = {
                        Text(
                            mDocumentFilePickerConfig?.askPermissionMessage ?: getString(
                                R.string.err_write_storage_permission,
                                getPermission().split(".").lastOrNull() ?: "",
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showAskDialog = false
                            permissionLauncher.launch(getPermission())
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
                Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: $uris")
                Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePaths")
                setSuccessResult(uris, filePath = filePaths)
            } else if (result.data?.data != null) {
                val data = result.data?.data
                val filePath = data?.let { FileUtils.getRealPath(this, it) }
                Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: ${data?.toString()}")
                Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePath")
                setSuccessResult(data, filePath)
            }
        } else {
            Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                .v(getString(R.string.err_document_pick_error))
            setCanceledResult(getString(R.string.err_document_pick_error))
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

        private fun getPermission(): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
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