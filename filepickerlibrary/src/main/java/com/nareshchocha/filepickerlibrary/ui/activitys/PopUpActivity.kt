package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.Orientation
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppDialog
import com.nareshchocha.filepickerlibrary.ui.components.item.PopupItem
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.setSuccessResult
import com.nareshchocha.filepickerlibrary.utilities.toArrayList

internal class PopUpActivity : ComponentActivity() {
    private val mPickerData: PickerData? by lazy {
        BundleCompat.getParcelable(
            intent.extras ?: Bundle.EMPTY,
            Const.BundleInternalExtras.PICKER_DATA,
            PickerData::class.java
        )
    }

    private val intentResultLauncher =
        registerForActivityResult(FilePickerResultContracts.AnyFilePicker()) { result ->
            if (!result.selectedFileUris.isNullOrEmpty()) {
                setSuccessResult(
                    result.selectedFileUris,
                    result?.selectedFilePaths?.toArrayList()
                )
            } else if (result.selectedFileUri != null) {
                setSuccessResult(result.selectedFileUri, result.selectedFilePath)
            } else {
                setCanceledResult(result.errorMessage)
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        AppDialog(
            modifier =
                Modifier.background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(config.cornerSize.dp)
                ),
            onDismissRequest = {
                setCanceledResult("User dismissed the popup")
                finish()
            }
        ) {
            PopupContent(config, items, onItemClick)
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
        ModalBottomSheet(
            onDismissRequest = {
                setCanceledResult("User dismissed the popup")
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
            PopupContent(config, items, onItemClick)
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
private fun PopupContent(
    config: PopUpConfig,
    items: List<BaseConfig>,
    onItemClick: (BaseConfig) -> Unit
) {
    val title = config.chooserTitle ?: stringResource(R.string.str_choose_option)
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

@Composable
private fun ItemList(
    orientation: Orientation,
    items: List<BaseConfig>,
    itemView: @Composable ((BaseConfig) -> Unit)? = null,
    onItemClick: (BaseConfig) -> Unit
) {
    if (orientation.isHorizontal()) {
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
        ) {
            items(items) {
                PopupItem(
                    item = it,
                    itemView = itemView,
                    onItemClick = onItemClick
                )
            }
        }
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
        ) {
            items(items) {
                PopupItem(
                    item = it,
                    itemView = itemView,
                    onItemClick = onItemClick
                )
            }
        }
    }
}
