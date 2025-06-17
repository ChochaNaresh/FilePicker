package com.nareshchocha.filepickerlibrary.ui.components.dialogs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.utilities.extensions.asString

@Composable
fun SettingDialog(
    permissions: List<String>,
    title: String? = null,
    message: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var show by remember {
        mutableStateOf(true)
    }
    if (show) {
        AppAlertDialog(
            onDismissRequest = {
                show = false
                onDismiss()
            },
            title =
                title ?: stringResource(R.string.err_permission_denied),
            message =
                message ?: stringResource(
                    R.string.err_write_storage_setting,
                    permissions.asString()
                ),
            onConfirm = {
                show = false
                onConfirm()
            },
            onDismiss = {
                show = false
                onDismiss()
            }
        )
    }
}