package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
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
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppRationaleDialog
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppSettingDialog
import com.nareshchocha.filepickerlibrary.utilities.MediaMultiplePermissionManager
import com.nareshchocha.filepickerlibrary.utilities.PermissionLists
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extensions.asString
import com.nareshchocha.filepickerlibrary.utilities.extensions.getActivityOrNull
import com.nareshchocha.filepickerlibrary.utilities.getMediaIntent
import com.nareshchocha.filepickerlibrary.utilities.setActivityResult
import com.nareshchocha.filepickerlibrary.utilities.setCanceledResult

internal class MediaFilePickerActivity : ComponentActivity() {
    private val mPickMediaConfig: PickMediaConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICK_MEDIA,
                PickMediaConfig::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.PICK_MEDIA) as PickMediaConfig?
        }
    }

    val mediaFilePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            setActivityResult(result.resultCode, result.data, false)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mPickMediaConfig == null) {
            setCanceledResult(getString(R.string.media_file_picker_config_null_error))
            return
        } else {
            setContent {
                StartMediaFilePicker()
            }
        }
    }

    @Composable
    fun StartMediaFilePicker() {
        val activity = LocalContext.current.getActivityOrNull() ?: this
        var showRationaleDialog by remember { mutableStateOf(false) }
        var showSettingDialog by remember { mutableStateOf(false) }
        val mMediaMultiplePermissionManager =
            MediaMultiplePermissionManager(
                activity = activity,
                permissions = PermissionLists.mediaFilePickerPermissions(),
                onPermissionsMissing = {
                    activity.setCanceledResult(
                        getString(
                            R.string.err_permission_missing,
                            it.asString()
                        )
                    )
                },
                onMultiplePermissionsGranted = {
                    mediaFilePickerLauncher()
                },
                onMultiplePermissionsDenied = {
                    showSettingDialog = true
                },
                onShowMultipleRationale = {
                    showRationaleDialog = true
                }
            )
        mMediaMultiplePermissionManager.Check()

        if (showSettingDialog) {
            ShowSettingDialog(
                permissions = mMediaMultiplePermissionManager.getDeniedPermissions(),
                onConfirm = {
                    showSettingDialog = false
                    mMediaMultiplePermissionManager.openAppSettings()
                },
                onDismiss = {
                    showSettingDialog = false
                    setPermissionDeniedResult(mMediaMultiplePermissionManager.getDeniedPermissions())
                }
            )
        }

        if (showRationaleDialog) {
            ShowRationaleDialog(
                onConfirm = {
                    showRationaleDialog = false
                    mMediaMultiplePermissionManager.permissionsCheck()
                },
                onDismiss = {
                    showRationaleDialog = false
                    setPermissionDeniedResult(mMediaMultiplePermissionManager.getRationalePermissions())
                }
            )
        }
    }

    fun mediaFilePickerLauncher() {
        if (mPickMediaConfig != null) {
            mediaFilePickerLauncher.launch(
                getMediaIntent(mPickMediaConfig!!)
            )
        } else {
            setCanceledResult(
                getString(R.string.media_file_picker_config_null_error)
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
            title = mPickMediaConfig?.settingPermissionTitle,
            message = mPickMediaConfig?.settingPermissionMessage,
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
                mPickMediaConfig?.askPermissionTitle
                    ?: stringResource(R.string.err_permissions_rationale_title),
            message =
                mPickMediaConfig?.askPermissionMessage ?: stringResource(
                    R.string.media_file_picker_permission_rationale
                ),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }

    companion object {
        fun getInstance(
            mContext: Context,
            mPickMediaConfig: PickMediaConfig?
        ): Intent =
            Intent(mContext, MediaFilePickerActivity::class.java).apply {
                mPickMediaConfig?.let {
                    putExtra(Const.BundleInternalExtras.PICK_MEDIA, it)
                }
            }
    }
}
