package com.nareshchocha.filepickerlibrary.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.Keep
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.Orientation
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
                mPopUpType = mPopUpConfig?.mPopUpType ?: PopUpType.BOTTOM_SHEET,
                mOrientation = mPopUpConfig?.mOrientation ?: Orientation.VERTICAL,
            )
            return this
        }

        @Keep
        fun addImageCapture(mImageCaptureConfig: ImageCaptureConfig? = null): Builder {
            listIntents.add(
                mImageCaptureConfig?: ImageCaptureConfig(),
            )
            return this
        }

        @Keep
        fun addVideoCapture(mVideoCaptureConfig: VideoCaptureConfig? = null): Builder {
            listIntents.add(
                mVideoCaptureConfig?:VideoCaptureConfig(),
            )
            return this
        }

        @Keep
        fun addPickMedia(mPickMediaConfig: PickMediaConfig? = null): Builder {
            listIntents.add(
                mPickMediaConfig ?: PickMediaConfig(),
            )
            return this
        }

        @Keep
        fun addPickDocumentFile(mDocumentFilePickerConfig: DocumentFilePickerConfig? = null): Builder {
            listIntents.add(mDocumentFilePickerConfig ?: DocumentFilePickerConfig())
            return this
        }

        @Keep
        fun imageCaptureBuild(mImageCaptureConfig: ImageCaptureConfig? = null): Intent =
            ImageCaptureActivity.getInstance(
                context,
                mImageCaptureConfig,
            )

        @Keep
        fun videoCaptureBuild(mVideoCaptureConfig: VideoCaptureConfig? = null): Intent =
            VideoCaptureActivity.getInstance(
                context,
                mVideoCaptureConfig,
            )

        @Keep
        fun pickMediaBuild(mPickMediaConfig: PickMediaConfig? = null): Intent =
            MediaFilePickerActivity.getInstance(
                context,
                mPickMediaConfig,
            )

        @Keep
        fun pickDocumentFileBuild(mDocumentFilePickerConfig: DocumentFilePickerConfig? = null): Intent =
            DocumentFilePickerActivity.getInstance(
                context,
                mDocumentFilePickerConfig,
            )

        @Keep
        fun build(): Intent = PopUpActivity.getInstance(
            context,
            PickerData(mPopUpConfig = mPopUpConfig, listIntents = listIntents),
        )
    }
}
