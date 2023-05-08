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
import com.nareshchocha.filepickerlibray.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibray.models.ImageAndVideo
import com.nareshchocha.filepickerlibray.models.ImageOnly
import com.nareshchocha.filepickerlibray.models.PickMediaConfig
import com.nareshchocha.filepickerlibray.models.PopUpType
import com.nareshchocha.filepickerlibray.models.PupConfig
import com.nareshchocha.filepickerlibray.models.VideoOnly
import com.nareshchocha.filepickerlibray.ui.FilePicker
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
                            mPickMediaType = ImageOnly,
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
                            mPickMediaType = VideoOnly,
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
                            mPickMediaType = ImageAndVideo,
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
                            mMimeTypes = listOf("application/pdf", "image/*"),
                        ),
                    ),
            )
        }

        binding.mbtAllOption.setOnClickListener {
            captureImageResultLauncher.launch(
                FilePicker.Builder(this)
                    .setPupConfig(
                        PupConfig(
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

    private val uriList: MutableList<Uri> = mutableListOf()
    private val mMediaAdapter: MediaAdapter by lazy {
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
                mMediaAdapter.notifyDataSetChanged()
                Timber.tag("FILE_RESULT").w(result.toString())
                Timber.tag("FILE_RESULT").w(result.data?.extras?.toString())
            } else {
                Timber.tag("FILE_PICKER_ERROR").e("capture Error")
            }
        }

    private fun Intent.getClipDataUris(): ArrayList<Uri> {
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
}
