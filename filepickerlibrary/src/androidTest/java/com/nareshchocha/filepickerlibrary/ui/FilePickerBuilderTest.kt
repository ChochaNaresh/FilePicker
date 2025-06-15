package com.nareshchocha.filepickerlibrary.ui

import android.content.Context
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class FilePickerBuilderTest {
    lateinit var appContext: Context

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /*@Test
    fun testImageCaptureBuild() {
        val mImageCaptureConfig = ImageCaptureConfig()
        val intentActivity = FilePicker.Builder(appContext).imageCaptureBuild(mImageCaptureConfig)
        val data =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intentActivity.getParcelableExtra(
                    Const.BundleInternalExtras.IMAGE_CAPTURE,
                    ImageCaptureConfig::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intentActivity.getParcelableExtra(Const.BundleInternalExtras.IMAGE_CAPTURE) as ImageCaptureConfig?
            }
        assertThat(data).isSameInstanceAs(mImageCaptureConfig)
    }*/
}
