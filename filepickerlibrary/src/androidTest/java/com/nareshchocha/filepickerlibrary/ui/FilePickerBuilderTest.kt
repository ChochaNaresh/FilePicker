package com.nareshchocha.filepickerlibrary.ui

import android.content.Context
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.nareshchocha.filepickerlibrary.models.BaseConfig
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickerData
import com.nareshchocha.filepickerlibrary.models.PopUpConfig
import com.nareshchocha.filepickerlibrary.models.PopUpType
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class FilePickerBuilderTest {
    lateinit var appContext: Context

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testPopupConfigAndListIntents() {
        val mPopUpConfig = PopUpConfig(
            chooserTitle = "test",
            mPopUpType = PopUpType.BOTTOM_SHEET,
            mOrientation = RecyclerView.VERTICAL,
        )
        val listIntents: List<BaseConfig> = ArrayList<BaseConfig>().apply {
            add(ImageCaptureConfig())
            add(VideoCaptureConfig())
            add(PickMediaConfig())
            add(DocumentFilePickerConfig())
        }

        val popupActivity = FilePicker.Builder(appContext).setPopUpConfig(mPopUpConfig)
            .addImageCapture(listIntents[0] as ImageCaptureConfig)
            .addVideoCapture(listIntents[1] as VideoCaptureConfig)
            .addPickMedia(listIntents[2] as PickMediaConfig)
            .addPickDocumentFile(listIntents[3] as DocumentFilePickerConfig).build()
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            popupActivity.getParcelableExtra(
                Const.BundleInternalExtras.PICKER_DATA,
                PickerData::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            popupActivity.getParcelableExtra(Const.BundleInternalExtras.PICKER_DATA) as PickerData?
        }
        assertThat(mPopUpConfig).isSameInstanceAs(data?.mPopUpConfig)
        assertThat(listIntents).isEqualTo(data?.listIntents)
    }

    @Test
    fun testImageCaptureBuild() {
        val mImageCaptureConfig = ImageCaptureConfig()
        val intentActivity = FilePicker.Builder(appContext).imageCaptureBuild(mImageCaptureConfig)
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentActivity.getParcelableExtra(
                Const.BundleInternalExtras.IMAGE_CAPTURE,
                ImageCaptureConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intentActivity.getParcelableExtra(Const.BundleInternalExtras.IMAGE_CAPTURE) as ImageCaptureConfig?
        }
        assertThat(data).isSameInstanceAs(mImageCaptureConfig)
    }

    @Test
    fun testVideoCaptureBuild() {
        val mVideoCaptureConfig = VideoCaptureConfig()
        val intentActivity = FilePicker.Builder(appContext).videoCaptureBuild(mVideoCaptureConfig)
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentActivity.getParcelableExtra(
                Const.BundleInternalExtras.VIDEO_CAPTURE,
                VideoCaptureConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intentActivity.getParcelableExtra(Const.BundleInternalExtras.VIDEO_CAPTURE) as VideoCaptureConfig?
        }
        assertThat(data).isSameInstanceAs(mVideoCaptureConfig)
    }

    @Test
    fun testPickMediaBuild() {
        val mPickMediaConfig = PickMediaConfig()
        val intentActivity = FilePicker.Builder(appContext).pickMediaBuild(mPickMediaConfig)
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentActivity.getParcelableExtra(
                Const.BundleInternalExtras.PICK_MEDIA,
                PickMediaConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intentActivity.getParcelableExtra(Const.BundleInternalExtras.PICK_MEDIA) as PickMediaConfig?
        }
        assertThat(data).isSameInstanceAs(mPickMediaConfig)
    }

    @Test
    fun testDocumentFilePickerBuild() {
        val mDocumentFilePickerConfig = DocumentFilePickerConfig()
        val intentActivity =
            FilePicker.Builder(appContext).pickDocumentFileBuild(mDocumentFilePickerConfig)
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intentActivity.getParcelableExtra(
                Const.BundleInternalExtras.PICK_DOCUMENT,
                DocumentFilePickerConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intentActivity.getParcelableExtra(Const.BundleInternalExtras.PICK_DOCUMENT) as DocumentFilePickerConfig?
        }
        assertThat(data).isSameInstanceAs(mDocumentFilePickerConfig)
    }
}
