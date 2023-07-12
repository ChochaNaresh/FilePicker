package com.nareshchocha.filepickerlibrary.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.PopUpType
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.ui.activitys.DocumentFilePickerActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.ImageCaptureActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.MediaFilePickerActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.PopUpActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.VideoCaptureActivity
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const

@Keep
class FilePicker private constructor() {

    @Keep
    class Builder(private val context: Context) {
        private val listIntents: ArrayList<BaseConfig> = ArrayList()
        private var mPopUpConfig: PopUpConfig? = null

        @Keep
        fun setPopUpConfig(mPopUpConfig: PopUpConfig? = null): Builder {
            this.mPopUpConfig = PopUpConfig(
                chooserTitle = mPopUpConfig?.chooserTitle ?: "Choose Option",
                layoutId = mPopUpConfig?.layoutId ?: R.layout.item_pop_up,
                mPopUpType = mPopUpConfig?.mPopUpType ?: PopUpType.BOTTOM_SHEET,
                mOrientation = mPopUpConfig?.mOrientation ?: RecyclerView.VERTICAL,
            )
            return this
        }

        @Keep
        fun addImageCapture(mImageCaptureConfig: ImageCaptureConfig? = null): Builder {
            listIntents.add(
                ImageCaptureConfig(
                    popUpIcon = mImageCaptureConfig?.popUpIcon ?: R.drawable.ic_camera,
                    popUpText = mImageCaptureConfig?.popUpText ?: "Camera",
                    mFolder = mImageCaptureConfig?.mFolder,
                    fileName = mImageCaptureConfig?.fileName ?: Const.DefaultPaths.defaultImageFile(),
                    askPermissionTitle = mImageCaptureConfig?.askPermissionTitle,
                    askPermissionMessage = mImageCaptureConfig?.askPermissionMessage,
                    settingPermissionTitle = mImageCaptureConfig?.settingPermissionTitle,
                    settingPermissionMessage = mImageCaptureConfig?.settingPermissionMessage,
                ),
            )
            return this
        }

        @Keep
        fun addVideoCapture(mVideoCaptureConfig: VideoCaptureConfig? = null): Builder {
            listIntents.add(
                VideoCaptureConfig(
                    popUpIcon = mVideoCaptureConfig?.popUpIcon ?: R.drawable.ic_video,
                    popUpText = mVideoCaptureConfig?.popUpText ?: "Video",
                    mFolder = mVideoCaptureConfig?.mFolder,
                    fileName = mVideoCaptureConfig?.fileName ?: Const.DefaultPaths.defaultVideoFile(),
                    maxSeconds = mVideoCaptureConfig?.maxSeconds,
                    maxSizeLimit = mVideoCaptureConfig?.maxSizeLimit,
                    isHighQuality = mVideoCaptureConfig?.isHighQuality,
                    askPermissionTitle = mVideoCaptureConfig?.askPermissionTitle,
                    askPermissionMessage = mVideoCaptureConfig?.askPermissionMessage,
                    settingPermissionTitle = mVideoCaptureConfig?.settingPermissionTitle,
                    settingPermissionMessage = mVideoCaptureConfig?.settingPermissionMessage,
                ),
            )
            return this
        }

        @Keep
        fun addPickMedia(mPickMediaConfig: PickMediaConfig? = null): Builder {
            listIntents.add(
                PickMediaConfig(
                    popUpIcon = mPickMediaConfig?.popUpIcon ?: R.drawable.ic_media,
                    popUpText = mPickMediaConfig?.popUpText ?: "Pick Media",
                    allowMultiple = mPickMediaConfig?.allowMultiple ?: false,
                    maxFiles = mPickMediaConfig?.maxFiles
                        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            MediaStore.getPickImagesMaxLimit()
                        } else {
                            Int.MAX_VALUE
                        },
                    mPickMediaType = mPickMediaConfig?.mPickMediaType ?: PickMediaType.ImageAndVideo,
                    askPermissionTitle = mPickMediaConfig?.askPermissionTitle,
                    askPermissionMessage = mPickMediaConfig?.askPermissionMessage,
                    settingPermissionTitle = mPickMediaConfig?.settingPermissionTitle,
                    settingPermissionMessage = mPickMediaConfig?.settingPermissionMessage,
                ),
            )
            return this
        }

        @Keep
        fun addPickDocumentFile(mDocumentFilePickerConfig: DocumentFilePickerConfig? = null): Builder {
            listIntents.add(
                DocumentFilePickerConfig(
                    popUpIcon = mDocumentFilePickerConfig?.popUpIcon ?: R.drawable.ic_file,
                    popUpText = mDocumentFilePickerConfig?.popUpText ?: "File Media",
                    allowMultiple = mDocumentFilePickerConfig?.allowMultiple ?: false,
                    maxFiles = mDocumentFilePickerConfig?.maxFiles
                        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            MediaStore.getPickImagesMaxLimit()
                        } else {
                            Int.MAX_VALUE
                        },
                    mMimeTypes = mDocumentFilePickerConfig?.mMimeTypes ?: listOf("*/*"),
                    askPermissionTitle = mDocumentFilePickerConfig?.askPermissionTitle,
                    askPermissionMessage = mDocumentFilePickerConfig?.askPermissionMessage,
                    settingPermissionTitle = mDocumentFilePickerConfig?.settingPermissionTitle,
                    settingPermissionMessage = mDocumentFilePickerConfig?.settingPermissionMessage,

                ),
            )
            return this
        }

        @Keep
        fun imageCaptureBuild(mImageCaptureConfig: ImageCaptureConfig? = null): Intent =
            ImageCaptureActivity.getInstance(
                context,
                ImageCaptureConfig(
                    popUpIcon = mImageCaptureConfig?.popUpIcon ?: R.drawable.ic_camera,
                    popUpText = mImageCaptureConfig?.popUpText ?: "Camera",
                    mFolder = mImageCaptureConfig?.mFolder,
                    fileName = mImageCaptureConfig?.fileName ?: Const.DefaultPaths.defaultImageFile(),
                    askPermissionTitle = mImageCaptureConfig?.askPermissionTitle,
                    askPermissionMessage = mImageCaptureConfig?.askPermissionMessage,
                    settingPermissionTitle = mImageCaptureConfig?.settingPermissionTitle,
                    settingPermissionMessage = mImageCaptureConfig?.settingPermissionMessage,
                ),
            )

        @Keep
        fun videoCaptureBuild(mVideoCaptureConfig: VideoCaptureConfig? = null): Intent =
            VideoCaptureActivity.getInstance(
                context,
                VideoCaptureConfig(
                    popUpIcon = mVideoCaptureConfig?.popUpIcon ?: R.drawable.ic_video,
                    popUpText = mVideoCaptureConfig?.popUpText ?: "Video",
                    mFolder = mVideoCaptureConfig?.mFolder,
                    fileName = mVideoCaptureConfig?.fileName ?: Const.DefaultPaths.defaultVideoFile(),
                    maxSeconds = mVideoCaptureConfig?.maxSeconds,
                    maxSizeLimit = mVideoCaptureConfig?.maxSizeLimit,
                    isHighQuality = mVideoCaptureConfig?.isHighQuality,
                    askPermissionTitle = mVideoCaptureConfig?.askPermissionTitle,
                    askPermissionMessage = mVideoCaptureConfig?.askPermissionMessage,
                    settingPermissionTitle = mVideoCaptureConfig?.settingPermissionTitle,
                    settingPermissionMessage = mVideoCaptureConfig?.settingPermissionMessage,
                ),
            )

        @Keep
        fun pickMediaBuild(mPickMediaConfig: PickMediaConfig? = null): Intent =
            MediaFilePickerActivity.getInstance(
                context,
                PickMediaConfig(
                    popUpIcon = mPickMediaConfig?.popUpIcon ?: R.drawable.ic_media,
                    popUpText = mPickMediaConfig?.popUpText ?: "Pick Media",
                    allowMultiple = mPickMediaConfig?.allowMultiple ?: false,
                    maxFiles = mPickMediaConfig?.maxFiles
                        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            MediaStore.getPickImagesMaxLimit()
                        } else {
                            Int.MAX_VALUE
                        },
                    mPickMediaType = mPickMediaConfig?.mPickMediaType ?: PickMediaType.ImageAndVideo,
                    askPermissionTitle = mPickMediaConfig?.askPermissionTitle,
                    askPermissionMessage = mPickMediaConfig?.askPermissionMessage,
                    settingPermissionTitle = mPickMediaConfig?.settingPermissionTitle,
                    settingPermissionMessage = mPickMediaConfig?.settingPermissionMessage,
                ),
            )

        @Keep
        fun pickDocumentFileBuild(mDocumentFilePickerConfig: DocumentFilePickerConfig? = null): Intent =
            DocumentFilePickerActivity.getInstance(
                context,
                DocumentFilePickerConfig(
                    popUpIcon = mDocumentFilePickerConfig?.popUpIcon ?: R.drawable.ic_file,
                    popUpText = mDocumentFilePickerConfig?.popUpText ?: "File Media",
                    allowMultiple = mDocumentFilePickerConfig?.allowMultiple ?: false,
                    maxFiles = mDocumentFilePickerConfig?.maxFiles
                        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            MediaStore.getPickImagesMaxLimit()
                        } else {
                            Int.MAX_VALUE
                        },
                    mMimeTypes = mDocumentFilePickerConfig?.mMimeTypes ?: listOf("*/*"),
                    askPermissionTitle = mDocumentFilePickerConfig?.askPermissionTitle,
                    askPermissionMessage = mDocumentFilePickerConfig?.askPermissionMessage,
                    settingPermissionTitle = mDocumentFilePickerConfig?.settingPermissionTitle,
                    settingPermissionMessage = mDocumentFilePickerConfig?.settingPermissionMessage,
                ),
            )

        @Keep
        fun build(): Intent = PopUpActivity.getInstance(
            context,
            PickerData(mPopUpConfig = mPopUpConfig, listIntents = listIntents),
        )
    }
}
