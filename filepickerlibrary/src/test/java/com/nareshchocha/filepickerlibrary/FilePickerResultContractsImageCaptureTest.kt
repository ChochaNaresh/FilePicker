package com.nareshchocha.filepickerlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nareshchocha.filepickerlibrary.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class FilePickerResultContractsImageCaptureTest {
    private lateinit var context: Context
    private lateinit var contract: FilePickerResultContracts.ImageCapture

    @Before
    fun setUp() {
        context = Mockito.mock(Context::class.java)
        contract = FilePickerResultContracts.ImageCapture()
    }

    @Test
    fun `createIntent returns correct intent`() {
        val config = ImageCaptureConfig(fileName = "test.jpg")
        val intent = contract.createIntent(context, config)
        assertNotNull(intent)
        assertEquals(
            "com.nareshchocha.filepickerlibrary.ui.activitys.ImageCaptureActivity",
            intent.component?.className
        )
    }

    @Test
    fun `parseResult returns error on null intent`() {
        val result = contract.parseResult(Activity.RESULT_OK, null)
        assertFalse(result.isSuccess())
        assertEquals("Image capture failed or cancelled", result.errorMessage)
    }

    @Test
    fun `parseResult returns error on cancelled result`() {
        val intent = Mockito.mock(Intent::class.java)
        val result = contract.parseResult(Activity.RESULT_CANCELED, intent)
        assertFalse(result.isSuccess())
        assertEquals("Image capture failed or cancelled", result.errorMessage)
    }

    @Test
    fun `parseResult returns success on valid result`() {
        val uri = Mockito.mock(Uri::class.java)
        val intent = Mockito.mock(Intent::class.java)
        Mockito.`when`(intent.data).thenReturn(uri)
        Mockito
            .`when`(intent.getStringExtra(Const.BundleExtras.FILE_PATH))
            .thenReturn("/path/to/image.jpg")

        val result = contract.parseResult(Activity.RESULT_OK, intent)
        assertTrue(result.isSuccess())
        assertEquals(uri, result.selectedFileUri)
        assertEquals("/path/to/image.jpg", result.selectedFilePath)
        assertNull(result.errorMessage)
    }
}
