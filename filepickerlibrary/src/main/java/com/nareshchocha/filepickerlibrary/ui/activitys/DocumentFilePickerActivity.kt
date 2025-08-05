package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.BundleCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppRationaleDialog
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppSettingDialog
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.PermissionLists
import com.nareshchocha.filepickerlibrary.utilities.SinglePermissionManager
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extensions.asString
import com.nareshchocha.filepickerlibrary.utilities.extensions.getActivityOrNull
import com.nareshchocha.filepickerlibrary.utilities.getClipDataUris
import com.nareshchocha.filepickerlibrary.utilities.getDocumentFilePick
import com.nareshchocha.filepickerlibrary.utilities.getFilePathList
import com.nareshchocha.filepickerlibrary.utilities.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.setSuccessResult

internal class DocumentFilePickerActivity : ComponentActivity() {
    private val mDocumentFilePickerConfig: DocumentFilePickerConfig? by lazy {
        BundleCompat.getParcelable(
            intent.extras ?: Bundle.EMPTY,
            Const.BundleInternalExtras.PICK_DOCUMENT,
            DocumentFilePickerConfig::class.java
        )
    }

    val documentFilePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                if (mDocumentFilePickerConfig?.allowMultiple == true && result.data?.clipData != null) {
                    val uris = result.data?.getClipDataUris()
                    val filePaths = uris?.getFilePathList(this)
                    setSuccessResult(uris, filePath = filePaths)
                } else if (result.data?.data != null) {
                    val data = result.data?.data
                    val filePath = data?.let { FileUtils.getRealPath(this, it) }
                    setSuccessResult(data, filePath)
                } else if (result.data?.clipData != null) {
                    val uri = result.data?.getClipDataUris()?.firstOrNull()
                    val filePath = uri?.let { FileUtils.getRealPath(this, it) }
                    setSuccessResult(uri, filePath)
                } else {
                    setCanceledResult(getString(R.string.document_file_picker_no_data_error))
                }
            } else {
                setCanceledResult("File Picker Result Error: ${result.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (mDocumentFilePickerConfig == null) {
            setCanceledResult(getString(R.string.document_file_picker_config_null_error))
            return
        } else {
            setContent {
                StaringDocumentFilePicker()
            }
        }
    }

    @Composable
    fun StaringDocumentFilePicker() {
        val activity = LocalContext.current.getActivityOrNull() ?: this
        var showRationaleDialog by remember { mutableStateOf(false) }
        var showSettingDialog by remember { mutableStateOf(false) }
        val mSinglePermissionManager =
            SinglePermissionManager(
                activity = activity,
                permission = PermissionLists.documentFilePickerPermissions() ?: "",
                onPermissionMissing = {
                    activity.setCanceledResult(
                        getString(
                            R.string.err_permission_missing,
                            it.asString()
                        )
                    )
                },
                onPermissionGranted = {
                    documentFilePickerLauncher()
                },
                onPermissionDenied = {
                    showSettingDialog = true
                },
                onShowRationale = {
                    showRationaleDialog = true
                }
            )
        mSinglePermissionManager.Check()

        if (showSettingDialog) {
            ShowSettingDialog(
                permissions = mSinglePermissionManager.getDeniedPermissions(),
                onConfirm = {
                    showSettingDialog = false
                    mSinglePermissionManager.openAppSettings()
                },
                onDismiss = {
                    showSettingDialog = false
                    setPermissionDeniedResult(mSinglePermissionManager.getDeniedPermissions())
                }
            )
        }

        if (showRationaleDialog) {
            ShowRationaleDialog(
                onConfirm = {
                    showRationaleDialog = false
                    mSinglePermissionManager.permissionsCheck()
                },
                onDismiss = {
                    showRationaleDialog = false
                    setPermissionDeniedResult(mSinglePermissionManager.getRationalePermissions())
                }
            )
        }
    }

    fun documentFilePickerLauncher() {
        if (mDocumentFilePickerConfig != null) {
            documentFilePickerLauncher.launch(
                getDocumentFilePick(mDocumentFilePickerConfig!!)
            )
        } else {
            setCanceledResult(
                getString(R.string.document_file_picker_config_null_error)
            )
        }
    }

    fun setPermissionDeniedResult(permissions: List<String>) {
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
            title = mDocumentFilePickerConfig?.settingPermissionTitle,
            message = mDocumentFilePickerConfig?.settingPermissionMessage,
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
                mDocumentFilePickerConfig?.askPermissionTitle
                    ?: stringResource(R.string.err_permissions_rationale_title),
            message =
                mDocumentFilePickerConfig?.askPermissionMessage ?: stringResource(
                    R.string.document_file_picker_permission_rationale
                ),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    companion object {
        fun getInstance(
            context: Context,
            mDocumentFilePickerConfig: DocumentFilePickerConfig?
        ): Intent =
            Intent(context, DocumentFilePickerActivity::class.java).apply {
                mDocumentFilePickerConfig?.let {
                    putExtra(Const.BundleInternalExtras.PICK_DOCUMENT, it)
                }
            }
    }
}
