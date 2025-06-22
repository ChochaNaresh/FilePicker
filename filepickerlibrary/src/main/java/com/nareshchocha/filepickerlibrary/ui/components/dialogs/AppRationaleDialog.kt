package com.nareshchocha.filepickerlibrary.ui.components.dialogs

import androidx.compose.runtime.Composable

@Composable
internal fun AppRationaleDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        message = message,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
