package com.nareshchocha.filepickerlibray.ui

import android.content.Context
import android.content.Intent
import com.nareshchocha.filepickerlibray.models.BaseConfig
import com.nareshchocha.filepickerlibray.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibray.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibray.models.PickMediaConfig
import com.nareshchocha.filepickerlibray.models.PickerData
import com.nareshchocha.filepickerlibray.models.PopUpConfig
import com.nareshchocha.filepickerlibray.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibray.ui.activitys.DocumentFilePickerActivity
import com.nareshchocha.filepickerlibray.ui.activitys.ImageCaptureActivity
import com.nareshchocha.filepickerlibray.ui.activitys.MediaFilePickerActivity
import com.nareshchocha.filepickerlibray.ui.activitys.PopUpActivity
import com.nareshchocha.filepickerlibray.ui.activitys.VideoCaptureActivity

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
