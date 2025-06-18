package com.nareshchocha.filepickerlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.FilePickerResult
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.ui.activitys.DocumentFilePickerActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.ImageCaptureActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.PopUpActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.VideoCaptureActivity
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extensions.getClipDataUris
import com.nareshchocha.filepickerlibrary.utilities.extensions.getFilePathList
import com.nareshchocha.filepickerlibrary.utilities.extensions.getMediaIntent

class FilePickerResultContracts private constructor() {
    companion object {
        var isLoggingEnabled: Boolean = false
    }

    class ImageCapture : ActivityResultContract<ImageCaptureConfig?, FilePickerResult>() {
        override fun createIntent(
            context: Context,
            input: ImageCaptureConfig?
        ): Intent = ImageCaptureActivity.getInstance(context, input ?: ImageCaptureConfig())

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "Image capture failed or cancelled")
            } else {
                FilePickerResult(
                    selectedFileUri = intent.data,
                    selectedFilePath = intent.getStringExtra(Const.BundleExtras.FILE_PATH)
                )
            }
    }

    class VideoCapture : ActivityResultContract<VideoCaptureConfig?, FilePickerResult>() {
        override fun createIntent(
            context: Context,
            input: VideoCaptureConfig?
        ): Intent = VideoCaptureActivity.getInstance(context, input ?: VideoCaptureConfig())

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "Video capture failed or cancelled")
            } else {
                FilePickerResult(
                    selectedFileUri = intent.data,
                    selectedFilePath = intent.getStringExtra(Const.BundleExtras.FILE_PATH)
                )
            }
    }

    class PickMedia : ActivityResultContract<PickMediaConfig?, FilePickerResult>() {
        var context: Context? = null

        override fun createIntent(
            context: Context,
            input: PickMediaConfig?
        ): Intent {
            this.context = context
            return context.getMediaIntent(input ?: PickMediaConfig())
        }
        // MediaFilePickerActivity.getInstance(context, input ?: PickMediaConfig())

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "Media selection failed or cancelled")
            } else {
                if (intent.clipData != null) {
                    val uris = intent.getClipDataUris()
                    val filePaths = uris.getFilePathList(context!!)
                    if (uris.isEmpty()) {
                        FilePickerResult(errorMessage = "No media selected")
                    } else {
                        FilePickerResult(
                            selectedFileUris = uris,
                            selectedFilePaths = filePaths
                        )
                    }
                } else if (intent.data != null) {
                    FilePickerResult(
                        selectedFileUri = intent.data,
                        selectedFilePath = intent.data?.let { FileUtils.getRealPath(context!!, it) }
                    )
                } else {
                    FilePickerResult(errorMessage = "No media selected")
                }

                /*if (intent.clipData != null) {
                    FilePickerResult(
                        selectedFileUris = intent.getClipDataUris(),
                        selectedFilePaths = intent.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)
                    )
                } else if (intent.data != null) {
                    FilePickerResult(
                        selectedFileUri = intent.data,
                        selectedFilePath = intent.getStringExtra(Const.BundleExtras.FILE_PATH)
                    )
                } else {
                    FilePickerResult(errorMessage = "No media selected")
                }*/
            }
    }

    class PickDocumentFile : ActivityResultContract<DocumentFilePickerConfig?, FilePickerResult>() {
        override fun createIntent(
            context: Context,
            input: DocumentFilePickerConfig?
        ): Intent = DocumentFilePickerActivity.getInstance(context, input ?: DocumentFilePickerConfig())

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "Document selection failed or cancelled")
            } else {
                if (intent.clipData != null) {
                    FilePickerResult(
                        selectedFileUris = intent.getClipDataUris(),
                        selectedFilePaths = intent.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)
                    )
                } else if (intent.data != null) {
                    FilePickerResult(
                        selectedFileUri = intent.data,
                        selectedFilePath = intent.getStringExtra(Const.BundleExtras.FILE_PATH)
                    )
                } else {
                    FilePickerResult(errorMessage = "No document selected")
                }
            }
    }

    class AllFilePicker : ActivityResultContract<PickerData?, FilePickerResult>() {
        override fun createIntent(
            context: Context,
            input: PickerData?
        ): Intent =
            PopUpActivity.getInstance(
                context,
                input ?: PickerData(
                    listIntents =
                        listOf(
                            ImageCaptureConfig(),
                            VideoCaptureConfig(),
                            PickMediaConfig(),
                            DocumentFilePickerConfig()
                        )
                )
            )

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "File selection failed or cancelled")
            } else {
                if (intent.clipData != null) {
                    FilePickerResult(
                        selectedFileUris = intent.getClipDataUris(),
                        selectedFilePaths = intent.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)
                    )
                } else if (intent.data != null) {
                    FilePickerResult(
                        selectedFileUri = intent.data,
                        selectedFilePath = intent.getStringExtra(Const.BundleExtras.FILE_PATH)
                    )
                } else {
                    FilePickerResult(errorMessage = "No file selected")
                }
            }
    }

    class AnyFilePicker : ActivityResultContract<BaseConfig?, FilePickerResult>() {
        override fun createIntent(
            context: Context,
            input: BaseConfig?
        ): Intent =
            when (input) {
                is ImageCaptureConfig -> ImageCaptureActivity.getInstance(context, input)
                is VideoCaptureConfig -> VideoCaptureActivity.getInstance(context, input)
                is PickMediaConfig -> PickMedia().createIntent(context, input)
                is DocumentFilePickerConfig ->
                    DocumentFilePickerActivity.getInstance(
                        context,
                        input
                    )

                else -> ImageCaptureActivity.getInstance(context, ImageCaptureConfig())
            }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "File selection failed or cancelled")
            } else {
                if (intent.clipData != null) {
                    FilePickerResult(
                        selectedFileUris = intent.getClipDataUris(),
                        selectedFilePaths = intent.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST)
                    )
                } else if (intent.data != null) {
                    FilePickerResult(
                        selectedFileUri = intent.data,
                        selectedFilePath = intent.getStringExtra(Const.BundleExtras.FILE_PATH)
                    )
                } else {
                    FilePickerResult(errorMessage = "No file selected")
                }
            }
    }
}
