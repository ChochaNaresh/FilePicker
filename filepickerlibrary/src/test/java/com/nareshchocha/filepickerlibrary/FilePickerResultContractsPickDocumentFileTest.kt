package com.nareshchocha.filepickerlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    sdk = [Build.VERSION_CODES.P]
)
class FilePickerResultContractsPickDocumentFileTest {
    private lateinit var context: Context
    private lateinit var contract: FilePickerResultContracts.PickDocumentFile

    @Before
    fun setUp() {
        context = Mockito.mock(Context::class.java)
        contract = FilePickerResultContracts.PickDocumentFile()
    }

    @Test
    fun `createIntent returns correct intent`() {
        val config = DocumentFilePickerConfig()
        val intent = contract.createIntent(context, config)
        assertNotNull(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Should be an ACTION_OPEN_DOCUMENT intent
            assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
        } else {
            // Should be the custom activity
            assertEquals(
                "com.nareshchocha.filepickerlibrary.ui.activitys.DocumentFilePickerActivity",
                intent.component?.className
            )
        }
    }

    @Test
    fun `parseResult returns error on null intent`() {
        val result = contract.parseResult(Activity.RESULT_OK, null)
        assertFalse(result.isSuccess())
        assertEquals("Document selection failed or cancelled", result.errorMessage)
    }

    @Test
    fun `parseResult returns error on cancelled result`() {
        val intent = Mockito.mock(Intent::class.java)
        val result = contract.parseResult(Activity.RESULT_CANCELED, intent)
        assertFalse(result.isSuccess())
        assertEquals("Document selection failed or cancelled", result.errorMessage)
    }

    @Test
    fun `parseResult returns success on valid result with data`() {
        val uri = Mockito.mock(Uri::class.java)
        val intent = Mockito.mock(Intent::class.java)
        // Ensure context is set before calling parseResult
        contract.createIntent(context, DocumentFilePickerConfig())
        Mockito.`when`(intent.data).thenReturn(uri)
        val result = contract.parseResult(Activity.RESULT_OK, intent)
        assertTrue(result.isSuccess())
        assertEquals(uri, result.selectedFileUri)
    }
}
