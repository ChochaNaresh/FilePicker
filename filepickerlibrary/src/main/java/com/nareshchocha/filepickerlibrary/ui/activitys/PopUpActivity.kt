package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.Orientation
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import com.nareshchocha.filepickerlibrary.ui.components.dialogs.AppDialog
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult

@OptIn(ExperimentalMaterial3Api::class)
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
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            setResult(result.resultCode, result.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (mPickerData == null) {
                setCanceledResult()
                finish()
                return@setContent
            }
            val context = LocalContext.current
            val items = mPickerData?.listIntents ?: emptyList()

            if (mPickerData?.mPopUpConfig?.mPopUpType?.isDialog() == true) {
                LoadDialogUI(
                    mPopUpConfig = mPickerData?.mPopUpConfig ?: PopUpConfig(),
                    items = items,
                    onItemClick = { item ->
                        handleItemClick(context, item)
                    }
                )
            } else {
                LoadBottomSheetUI(
                    mPopUpConfig = mPickerData?.mPopUpConfig ?: PopUpConfig(),
                    items = items,
                    onItemClick = { item ->
                        handleItemClick(context, item)
                    }
                )
            }
        }
    }

    @Composable
    fun LoadDialogUI(
        mPopUpConfig: PopUpConfig,
        items: List<BaseConfig> = emptyList(),
        onItemClick: (BaseConfig) -> Unit
    ) {
        val title = mPopUpConfig.chooserTitle ?: stringResource(R.string.str_choose_option)
        AppDialog(
            modifier =
                Modifier
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(mPopUpConfig.cornerSize.dp)
                    ),
            onDismissRequest = {
                setCanceledResult()
                finish()
            }
        ) {
            Column {
                if (mPopUpConfig.title != null) {
                    mPopUpConfig.title(title)
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                ItemList(
                    orientation = mPopUpConfig.mOrientation ?: Orientation.VERTICAL,
                    items = items,
                    item = mPopUpConfig.item,
                    onItemClick = onItemClick
                )
            }
        }
    }

    @Composable
    fun LoadBottomSheetUI(
        mPopUpConfig: PopUpConfig,
        items: List<BaseConfig> = emptyList(),
        onItemClick: (BaseConfig) -> Unit
    ) {
        val sheetState = rememberModalBottomSheetState()
        val title = mPopUpConfig.chooserTitle ?: stringResource(R.string.str_choose_option)
        ModalBottomSheet(
            onDismissRequest = {
                setCanceledResult()
                finish()
            },
            dragHandle = null,
            sheetState = sheetState,
            shape =

                RoundedCornerShape(
                    topStart = mPopUpConfig.cornerSize.dp,
                    topEnd = mPopUpConfig.cornerSize.dp
                )
        ) {
            Column(modifier = Modifier) {
                if (mPopUpConfig.title != null) {
                    mPopUpConfig.title(title)
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                ItemList(
                    orientation = mPopUpConfig.mOrientation ?: Orientation.VERTICAL,
                    items = items,
                    item = mPopUpConfig.item,
                    onItemClick = onItemClick
                )
            }
        }
    }

    @Composable
    private fun ItemList(
        modifier: Modifier = Modifier,
        orientation: Orientation,
        items: List<BaseConfig>,
        item: @Composable ((item: BaseConfig) -> Unit)? = null,
        onItemClick: (BaseConfig) -> Unit
    ) {
        if (orientation.isHorizontal()) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .horizontalScroll(rememberScrollState())
            ) {
                ItemList(items, item, onItemClick)
            }
        } else {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .verticalScroll(rememberScrollState())
            ) {
                ItemList(items, item, onItemClick)
            }
        }
    }

    @Composable
    private fun ItemList(
        items: List<BaseConfig>,
        itemView: @Composable ((item: BaseConfig) -> Unit)? = null,
        onItemClick: (BaseConfig) -> Unit
    ) {
        items.forEach { item ->
            if (itemView != null) {
                itemView(item)
            } else {
                TextButton(
                    onClick = { onItemClick(item) }
                ) {
                    Row {
                        val iconRes = item.popUpIcon
                        if (iconRes != null && iconRes != 0) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .padding(end = 8.dp)
                                        .height(24.dp)
                            )
                        }
                        Text(item.popUpText ?: "")
                    }
                }
            }
        }
    }

    private fun handleItemClick(
        context: Context,
        item: BaseConfig
    ) {
        when (item) {
            is ImageCaptureConfig -> {
                intentResultLauncher.launch(FilePicker.Builder(context).imageCaptureBuild(item))
            }

            is VideoCaptureConfig -> {
                intentResultLauncher.launch(FilePicker.Builder(context).videoCaptureBuild(item))
            }

            is PickMediaConfig -> {
                intentResultLauncher.launch(FilePicker.Builder(context).pickMediaBuild(item))
            }

            is DocumentFilePickerConfig -> {
                intentResultLauncher.launch(FilePicker.Builder(context).pickDocumentFileBuild(item))
            }
        }
    }

    companion object {
        fun getInstance(
            mContext: Context,
            mPickerData: PickerData?
        ): Intent {
            val filePickerIntent = Intent(mContext, PopUpActivity::class.java)
            mPickerData?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICKER_DATA, it)
            }
            filePickerIntent.flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            return filePickerIntent
        }
    }
}
