package com.nareshchocha.filepickerlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
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
import com.nareshchocha.filepickerlibrary.ui.activitys.MediaFilePickerActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.PopUpActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.VideoCaptureActivity
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.getClipDataUris
import com.nareshchocha.filepickerlibrary.utilities.getDocumentFilePick
import com.nareshchocha.filepickerlibrary.utilities.getFilePathList
import com.nareshchocha.filepickerlibrary.utilities.getMediaIntent

/**
 * A collection of ActivityResultContracts for different file picking operations.
 * This class provides various contracts for capturing images, videos, selecting media,
 * documents, and other file types.
 */
class FilePickerResultContracts private constructor() {
    companion object {
        /**
         * Flag to enable or disable logging within the file picker contracts.
         */
        var isLoggingEnabled: Boolean = false
    }

    /**
     * Contract for capturing images using the device camera.
     * Returns a FilePickerResult that contains the URI and file path of the captured image.
     */
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

    /**
     * Contract for capturing videos using the device camera.
     * Returns a FilePickerResult that contains the URI and file path of the captured video.
     */
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

    /**
     * Contract for picking media files (images, videos, audio) from the device.
     * Returns a FilePickerResult that contains the URIs and file paths of the selected media.
     */
    class PickMedia : ActivityResultContract<PickMediaConfig?, FilePickerResult>() {
        private var context: Context? = null

        override fun createIntent(
            context: Context,
            input: PickMediaConfig?
        ): Intent {
            this.context = context
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getMediaIntent(
                    input ?: PickMediaConfig()
                )
            } else {
                MediaFilePickerActivity.getInstance(context, input ?: PickMediaConfig())
            }
        }

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
            }
    }

    /**
     * Contract for picking document files from the device.
     * Returns a FilePickerResult that contains the URIs and file paths of the selected documents.
     */
    class PickDocumentFile : ActivityResultContract<DocumentFilePickerConfig?, FilePickerResult>() {
        private var context: Context? = null

        override fun createIntent(
            context: Context,
            input: DocumentFilePickerConfig?
        ): Intent {
            this.context = context
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getDocumentFilePick(input ?: DocumentFilePickerConfig())
            } else {
                DocumentFilePickerActivity.getInstance(context, input ?: DocumentFilePickerConfig())
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "Document selection failed or cancelled")
            } else {
                if (intent.clipData != null) {
                    val uris = intent.getClipDataUris()
                    val filePaths = uris.getFilePathList(context!!)
                    if (uris.isEmpty()) {
                        FilePickerResult(errorMessage = "No document selected")
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
                    FilePickerResult(errorMessage = "No document selected")
                }
            }
    }

    /**
     * Contract for displaying a popup with multiple file picker options.
     * Returns a FilePickerResult based on the selected option and resulting file(s).
     */
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

    /**
     * Contract for picking any file type based on the provided configuration.
     * Delegates to the appropriate specific contract based on the config type.
     * Returns a FilePickerResult based on the selected file(s).
     */
    class AnyFilePicker : ActivityResultContract<BaseConfig?, FilePickerResult>() {
        private var baseConfig: BaseConfig? = null

        override fun createIntent(
            context: Context,
            input: BaseConfig?
        ): Intent {
            baseConfig = input ?: ImageCaptureConfig()
            return when (input) {
                is ImageCaptureConfig -> ImageCapture().createIntent(context, input)
                is VideoCaptureConfig -> VideoCapture().createIntent(context, input)
                is PickMediaConfig -> PickMedia().createIntent(context, input)
                is DocumentFilePickerConfig -> PickDocumentFile().createIntent(context, input)
                else -> ImageCapture().createIntent(context, ImageCaptureConfig())
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): FilePickerResult =
            if (intent == null || resultCode != Activity.RESULT_OK) {
                FilePickerResult(errorMessage = "File selection failed or cancelled")
            } else {
                when (baseConfig) {
                    is ImageCaptureConfig -> ImageCapture().parseResult(resultCode, intent)
                    is VideoCaptureConfig -> VideoCapture().parseResult(resultCode, intent)
                    is PickMediaConfig -> PickMedia().parseResult(resultCode, intent)
                    is DocumentFilePickerConfig ->
                        PickDocumentFile().parseResult(
                            resultCode,
                            intent
                        )
                    else -> FilePickerResult(errorMessage = "Unknown file type")
                }
            }
    }
}
