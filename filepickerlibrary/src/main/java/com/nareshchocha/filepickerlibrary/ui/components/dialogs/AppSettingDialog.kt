package com.nareshchocha.filepickerlibrary.ui.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.utilities.extensions.asString

@Composable
internal fun AppSettingDialog(
    permissions: List<String>,
    title: String? = null,
    message: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title =
            title ?: stringResource(R.string.err_permission_denied),
        message =
            message ?: stringResource(
                R.string.err_write_storage_setting,
                permissions.asString()
            ),
        confirmText = stringResource(R.string.str_go_to_setting),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
