package com.nareshchocha.filepickerlibray.ui.activitys

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nareshchocha.filepickerlibray.R
import com.nareshchocha.filepickerlibray.databinding.ActivityPopUpBinding
import com.nareshchocha.filepickerlibray.models.BaseConfig
import com.nareshchocha.filepickerlibray.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibray.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibray.models.PickMediaConfig
import com.nareshchocha.filepickerlibray.models.PickerData
import com.nareshchocha.filepickerlibray.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibray.ui.FilePicker
import com.nareshchocha.filepickerlibray.ui.adapter.PopUpAdapter
import com.nareshchocha.filepickerlibray.utilities.appConst.Const
import com.nareshchocha.filepickerlibray.utilities.extentions.setCanceledResult

class PopUpActivity : AppCompatActivity() {
    private val binding: ActivityPopUpBinding by lazy {
        ActivityPopUpBinding.inflate(layoutInflater)
    }
    private val mPickerData: PickerData? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Const.BundleExtras.PICKER_DATA, PickerData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleExtras.PICKER_DATA) as PickerData?
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAdapter()
        binding.tbToolbar.title = if (!mPickerData?.mPupConfig?.chooserTitle.isNullOrEmpty()) {
            mPickerData?.mPupConfig?.chooserTitle
        } else {
            getString(R.string.str_choose_option)
        }
        if (mPickerData?.mPupConfig?.mPopUpType?.isDialog() == true) {
            binding.root.radius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Const.CARD_RADIUS,
                resources.displayMetrics,
            )
            setContentView(binding.root)
        } else {
            showBottomSheetDialog()
        }
    }

    private fun setAdapter() {
        val linearLayoutManager = LinearLayoutManager(
            this,
            if (mPickerData?.mPupConfig?.mOrientation != null) {
                mPickerData?.mPupConfig?.mOrientation!!
            } else {
                RecyclerView.VERTICAL
            },
            false,
        )
        val popUpAdapter = PopUpAdapter(
            mPickerData?.mPupConfig?.layoutId ?: R.layout.item_pop_up,
            items = getAdapterItemList(),
            itemClicked = { item, _ ->
                mBottomSheet.dismiss()
                when (item) {
                    is ImageCaptureConfig -> {
                        intentResultLauncher.launch(
                            FilePicker.Builder(this).imageCaptureBuild(item),
                        )
                    }

                    is VideoCaptureConfig -> {
                        intentResultLauncher.launch(
                            FilePicker.Builder(this).videoCaptureBuild(item),

                        )
                    }

                    is PickMediaConfig -> {
                        intentResultLauncher.launch(
                            FilePicker.Builder(this).pickMediaBuild(item),

                        )
                    }

                    is DocumentFilePickerConfig -> {
                        intentResultLauncher.launch(
                            FilePicker.Builder(this).pickDocumentFileBuild(item),

                        )
                    }
                }
            },
        )
        binding.rvItems.layoutManager = linearLayoutManager
        binding.rvItems.adapter = popUpAdapter
    }

    private fun getAdapterItemList(): List<BaseConfig> {
        return mPickerData?.listIntents ?: emptyList()
    }

    private val mBottomSheet: BottomSheetDialog by lazy {
        BottomSheetDialog(this, R.style.Theme_FilePicker_BottomSheetDialog).apply {

            setContentView(binding.root)
            setOnCancelListener {
                setCanceledResult()
            }
        }
    }

    private fun showBottomSheetDialog() {
        mBottomSheet.window?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.transparent,
            ),
        )
        mBottomSheet.show()
    }

    private val intentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            setResult(result.resultCode, result.data)
            finish()
        }

    companion object {
        fun getInstance(mContext: Context, mPickerData: PickerData?): Intent {
            val filePickerIntent = Intent(mContext, PopUpActivity::class.java)
            mPickerData?.let {
                filePickerIntent.putExtra(Const.BundleExtras.PICKER_DATA, it)
            }
            return filePickerIntent
        }
    }
}
