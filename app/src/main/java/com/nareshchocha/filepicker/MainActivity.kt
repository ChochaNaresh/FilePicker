package com.nareshchocha.filepicker

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.nareshchocha.filepicker.databinding.ActivityMainBinding
import com.nareshchocha.filepickerlibray.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibray.models.ImageAndVideo
import com.nareshchocha.filepickerlibray.models.ImageOnly
import com.nareshchocha.filepickerlibray.models.PickMediaConfig
import com.nareshchocha.filepickerlibray.models.PopUpType
import com.nareshchocha.filepickerlibray.models.PupConfig
import com.nareshchocha.filepickerlibray.models.VideoOnly
import com.nareshchocha.filepickerlibray.ui.FilePicker
import com.nareshchocha.filepickerlibray.utilities.appConst.Const
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
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

    private val captureImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null && result.resultCode == RESULT_OK) {
                binding.ivImage.visibility = View.VISIBLE
                binding.ivImage.setImageURI(result.data?.data)
                Timber.tag(Const.LogTag.FILE_RESULT).w(result.toString())
                Timber.tag(Const.LogTag.FILE_RESULT).w(result.data?.extras?.toString())
            } else {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR).e("capture Error")
            }
        }
}
