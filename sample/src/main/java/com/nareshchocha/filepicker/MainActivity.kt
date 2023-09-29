package com.nareshchocha.filepicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nareshchocha.filepicker.adapter.MediaAdapter
import com.nareshchocha.filepicker.databinding.ActivityMainBinding
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.PopUpType
import com.nareshchocha.filepickerlibrary.ui.FilePicker
import org.jetbrains.annotations.VisibleForTesting
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setAdapter()
        binding.mbtCaptureImage.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .imageCaptureBuild(),
            )
        }
        binding.mbtCaptureVideo.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .videoCaptureBuild(),
            )
        }

        binding.mbtPickImages.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .pickMediaBuild(
                        PickMediaConfig(
                            mPickMediaType = PickMediaType.ImageOnly,
                            allowMultiple = true,
                        ),
                    ),
            )
        }

        binding.mbtPickVideo.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .pickMediaBuild(
                        PickMediaConfig(
                            mPickMediaType = PickMediaType.VideoOnly,
                            allowMultiple = true,
                            maxFiles = 3,
                        ),
                    ),
            )
        }

        binding.mbtPickImageVideo.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .pickMediaBuild(
                        PickMediaConfig(
                            mPickMediaType = PickMediaType.ImageAndVideo,
                            allowMultiple = true,
                        ),
                    ),
            )
        }

        binding.mbtPDF.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .pickDocumentFileBuild(
                        DocumentFilePickerConfig(
                            allowMultiple = true,
                            mMimeTypes = listOf("application/pdf"),
                        ),
                    ),
            )
        }

        binding.mbtAllOption.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .setPopUpConfig(
                        PopUpConfig(
                            mPopUpType = PopUpType.BOTTOM_SHEET,
                            mOrientation = RecyclerView.VERTICAL,
                            chooserTitle = "Choose Profile",
                        ),
                    )
                    .addPickDocumentFile()
                    .addImageCapture()
                    .addVideoCapture()
                    .addPickMedia()
                    .build(),
            )
        }
    }

    @VisibleForTesting
    val uriList: MutableList<Uri> = mutableListOf()

    val mMediaAdapter: MediaAdapter by lazy {
        MediaAdapter(
            this,
            items = uriList,
        )
    }

    private fun setAdapter() {
        val linearLayoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false,
        )
        binding.rvMedia.layoutManager = linearLayoutManager
        binding.rvMedia.adapter = mMediaAdapter
    }

    private val captureImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result != null && result.resultCode == RESULT_OK) {
                uriList.clear()
                if (result.data?.data != null) {
                    result.data?.data?.let {
                        uriList.add(it)
                    }
                } else {
                    val listData = result.data?.getClipDataUris()
                    // val listData = result.data?.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)
                    listData?.let { uriList.addAll(it) }
                }
                mMediaAdapter.notifyItemRangeChanged(0, uriList.size)
                Timber.tag("FILE_RESULT").v(result.toString())
                Timber.tag("FILE_RESULT").v(result.data?.extras?.toString())
            } else {
                Timber.tag("FILE_PICKER_ERROR").v("capture Error")
            }
        }
}

fun Intent.getClipDataUris(): ArrayList<Uri> {
    val resultSet = LinkedHashSet<Uri>()
    data?.let { data ->
        resultSet.add(data)
    }
    val clipData = clipData
    if (clipData == null && resultSet.isEmpty()) {
        return ArrayList()
    } else if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            val uri = clipData.getItemAt(i).uri
            if (uri != null) {
                resultSet.add(uri)
            }
        }
    }
    return ArrayList(resultSet)
}
