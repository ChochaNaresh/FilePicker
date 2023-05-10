package com.nareshchocha.filepickerlibrary.ui

import android.content.Context
import android.content.Intent
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.ui.activitys.DocumentFilePickerActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.ImageCaptureActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.MediaFilePickerActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.PopUpActivity
import com.nareshchocha.filepickerlibrary.ui.activitys.VideoCaptureActivity

open class FilePicker {

    class Builder(private val context: Context) {
        private val listIntents: ArrayList<BaseConfig> = ArrayList()
        private var mPopUpConfig: PopUpConfig? = null

        fun setPopUpConfig(mPopUpConfig: PopUpConfig? = null): Builder {
            this.mPopUpConfig = mPopUpConfig ?: PopUpConfig()
            return this
        }

        fun addImageCapture(mImageCaptureConfig: ImageCaptureConfig? = null): Builder {
            listIntents.add(mImageCaptureConfig ?: ImageCaptureConfig())
            return this
        }

        fun addVideoCapture(mVideoCaptureConfig: VideoCaptureConfig? = null): Builder {
            listIntents.add(mVideoCaptureConfig ?: VideoCaptureConfig())
            return this
        }

        fun addPickMedia(mPickMediaConfig: PickMediaConfig? = null): Builder {
            listIntents.add(mPickMediaConfig ?: PickMediaConfig())
            return this
        }

        fun addPickDocumentFile(mDocumentFilePickerConfig: DocumentFilePickerConfig? = null): Builder {
            listIntents.add(mDocumentFilePickerConfig ?: DocumentFilePickerConfig())
            return this
        }

        fun imageCaptureBuild(mImageCaptureConfig: ImageCaptureConfig? = null): Intent =
            ImageCaptureActivity.getInstance(context, mImageCaptureConfig ?: ImageCaptureConfig())

        fun videoCaptureBuild(mVideoCaptureConfig: VideoCaptureConfig? = null): Intent =
            VideoCaptureActivity.getInstance(context, mVideoCaptureConfig ?: VideoCaptureConfig())

        fun pickMediaBuild(mPickMediaConfig: PickMediaConfig? = null): Intent =
            MediaFilePickerActivity.getInstance(context, mPickMediaConfig ?: PickMediaConfig())

        fun pickDocumentFileBuild(mDocumentFilePickerConfig: DocumentFilePickerConfig? = null): Intent =
            DocumentFilePickerActivity.getInstance(
                context,
                mDocumentFilePickerConfig ?: DocumentFilePickerConfig(),
            )

        fun build(): Intent = PopUpActivity.getInstance(
            context,
            PickerData(mPopUpConfig = mPopUpConfig, listIntents = listIntents),
        )
    }
}
