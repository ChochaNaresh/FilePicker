package com.nareshchocha.filepickerlibrary.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult

@OptIn(ExperimentalMaterial3Api::class)
internal class PopUpActivity : ComponentActivity() {
    private val mPickerData: PickerData? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICKER_DATA,
                PickerData::class.java,
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
            val context = LocalContext.current
            val showDialog =
                remember { mutableStateOf(mPickerData?.mPopUpConfig?.mPopUpType?.isDialog() == true) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val items = mPickerData?.listIntents ?: emptyList()
            val title = mPickerData?.mPopUpConfig?.chooserTitle.takeUnless { it.isNullOrEmpty() }
                ?: context.getString(R.string.str_choose_option)

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        setCanceledResult()
                        finish()
                    },
                    title = { Text(title) },
                    text = {
                        ItemList(
                            items = items,
                            onItemClick = { item ->
                                handleItemClick(context, item)
                            }
                        )
                    },
                    confirmButton = {},
                    shape = RoundedCornerShape(Const.CARD_RADIUS.dp)
                )
            } else {
                ModalBottomSheet(
                    onDismissRequest = {
                        setCanceledResult()
                        finish()
                    },
                    dragHandle = null,
                    sheetState = sheetState,
                    shape = RoundedCornerShape(
                        topStart = Const.CARD_RADIUS.dp,
                        topEnd = Const.CARD_RADIUS.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        ItemList(
                            items = items,
                            onItemClick = { item ->
                                handleItemClick(context, item)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ItemList(
        items: List<BaseConfig>,
        onItemClick: (BaseConfig) -> Unit
    ) {
        Column {
            items.forEach { item ->
                TextButton(
                    onClick = { onItemClick(item) },
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                ) {
                    Row {
                        val iconRes = item.popUpIcon
                        if (iconRes != null && iconRes != 0) {
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                modifier = Modifier
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

    private fun handleItemClick(context: Context, item: BaseConfig) {
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
        fun getInstance(mContext: Context, mPickerData: PickerData?): Intent {
            val filePickerIntent = Intent(mContext, PopUpActivity::class.java)
            mPickerData?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICKER_DATA, it)
            }
            filePickerIntent.flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            return filePickerIntent
        }
    }
}