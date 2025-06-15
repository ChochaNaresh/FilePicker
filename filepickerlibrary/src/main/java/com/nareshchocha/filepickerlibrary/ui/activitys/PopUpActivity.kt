package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.Orientation
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppDialog
import com.nareshchocha.filepickerlibrary.ui.components.item.PopupItem
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extensions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extensions.setSuccessResult
import com.nareshchocha.filepickerlibrary.utilities.extensions.toArrayList

internal class PopUpActivity : ComponentActivity() {
    private val mPickerData: PickerData? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICKER_DATA,
                PickerData::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.PICKER_DATA) as PickerData?
        }
    }

    private val intentResultLauncher =
        registerForActivityResult(FilePickerResultContracts.AnyFilePicker()) { result ->
            when {
                !result.selectedFileUris.isNullOrEmpty() && !result.selectedFilePaths.isNullOrEmpty() ->
                    setSuccessResult(
                        result.selectedFileUris,
                        result.selectedFilePaths.toArrayList()
                    )

                result.selectedFileUri != null && result.selectedFilePath != null ->
                    setSuccessResult(result.selectedFileUri, result.selectedFilePath)

                else -> setCanceledResult(result.errorMessage)
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pickerData = mPickerData
            if (pickerData == null) {
                setCanceledResult()
                finish()
                return@setContent
            }
            val config = pickerData.mPopUpConfig ?: PopUpConfig()
            val items = pickerData.listIntents
            if (config.mPopUpType?.isDialog() == true) {
                LoadDialogUI(config, items) { intentResultLauncher.launch(it) }
            } else {
                LoadBottomSheetUI(config, items) { intentResultLauncher.launch(it) }
            }
        }
    }

    @Composable
    fun LoadDialogUI(
        config: PopUpConfig,
        items: List<BaseConfig>,
        onItemClick: (BaseConfig) -> Unit
    ) {
        val title = config.chooserTitle ?: stringResource(R.string.str_choose_option)
        AppDialog(
            modifier =
                Modifier.background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(config.cornerSize.dp)
                ),
            onDismissRequest = {
                setCanceledResult()
                finish()
            }
        ) {
            Column {
                config.title?.invoke(title) ?: Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                ItemList(
                    orientation = config.mOrientation ?: Orientation.VERTICAL,
                    items = items,
                    itemView = config.item,
                    onItemClick = onItemClick
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoadBottomSheetUI(
        config: PopUpConfig,
        items: List<BaseConfig>,
        onItemClick: (BaseConfig) -> Unit
    ) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        val title = config.chooserTitle ?: stringResource(R.string.str_choose_option)
        ModalBottomSheet(
            onDismissRequest = {
                setCanceledResult()
                finish()
            },
            dragHandle = null,
            sheetState = sheetState,
            shape =
                RoundedCornerShape(
                    topStart = config.cornerSize.dp,
                    topEnd = config.cornerSize.dp
                )
        ) {
            Column {
                config.title?.invoke(title) ?: Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                ItemList(
                    orientation = config.mOrientation ?: Orientation.VERTICAL,
                    items = items,
                    itemView = config.item,
                    onItemClick = onItemClick
                )
            }
        }
    }

    companion object {
        fun getInstance(
            context: Context,
            pickerData: PickerData?
        ): Intent =
            Intent(context, PopUpActivity::class.java).apply {
                pickerData?.let { putExtra(Const.BundleInternalExtras.PICKER_DATA, it) }
                flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            }
    }
}

@Composable
private fun ItemList(
    orientation: Orientation,
    items: List<BaseConfig>,
    itemView: @Composable ((BaseConfig) -> Unit)? = null,
    onItemClick: (BaseConfig) -> Unit
) {
    val scrollState = rememberScrollState()
    if (orientation.isHorizontal()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .horizontalScroll(scrollState)
        ) {
            items.forEach {
                PopupItem(
                    item = it,
                    itemView = itemView,
                    onItemClick = onItemClick
                )
            }
        }
    } else {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .verticalScroll(scrollState)
        ) {
            items.forEach {
                PopupItem(
                    item = it,
                    itemView = itemView,
                    onItemClick = onItemClick
                )
            }
        }
    }
}
