package com.nareshchocha.filepicker

import android.content.res.Resources
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class AppUITest {

    private lateinit var device: UiDevice
    private lateinit var mResources: Resources

    @get:Rule
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(
        MainActivity::class.java,
    )

    @Before
    fun startMainActivityFromHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mResources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        // startAppAndWaitLaunch()
    }

    /* @Before
     fun tearDown() {
         try {
             InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                 TestConst.BASIC_SAMPLE_PACKAGE,
                 Manifest.permission.CAMERA,
             )
             InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                 TestConst.BASIC_SAMPLE_PACKAGE,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE,
             )
         } catch (e: Exception) {
             e.stackTraceToString()
         }
     }*/

    private fun clickPermissionAllow() {
        device.clickAllAllowButtons()
        device.clickAllAllowOnlyThisTimeButtons()
        device.clickAllAllowLocationButtons()
    }

    private fun clickPermissionDeny() {
        device.clickAllDenyButtons()
        val okButton = device.findObject(
            By.text("OK").clazz(TestConst.BUTTON_CLASS_NAME),
        )
        okButton?.clickAndWait()
        device.clickAllDenyButtons(true)
        val gotoSettingButton = device.findObject(
            By.text("GO TO SETTING").clazz(TestConst.BUTTON_CLASS_NAME),
        )
        TestCase.assertNotNull(gotoSettingButton)
        TestCase.assertEquals(gotoSettingButton.isEnabled, true)
        val cancelButton = device.findObject(
            By.text("CANCEL").clazz(TestConst.BUTTON_CLASS_NAME),
        )
        TestCase.assertNotNull(cancelButton)
        cancelButton.clickAndWait()
    }

    private fun checkItemAddedOnList(expectedCount: Int) {
        activityScenarioRule.scenario.onActivity {
            Truth.assertThat(it.uriList.size == expectedCount).isTrue()
            Truth.assertThat(it.mMediaAdapter.itemCount == expectedCount).isTrue()
        }
    }

    private fun successCameraCapture(isVideo: Boolean = false) {
        val shutterButton =
            device.findObject(
                By.res(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        "com.android.camera2:id/shutter_button"
                    } else {
                        "com.android.camera:id/shutter_button"
                    },
                )
                    .text(Pattern.compile(""))
                    .pkg("com.android.camera2"),
            )

        if (shutterButton.isEnabled) {
            shutterButton.clickAndWait()
        }
        if (isVideo) {
            shutterButton.clickAndWait()
        }
        val doneButton = device.findObject(
            By.res(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    "com.android.camera2:id/done_button"
                } else {
                    "com.android.camera:id/btn_done"
                },
            ).text(Pattern.compile("")),
        )
        if (doneButton != null) {
            doneButton.clickAndWait()
        } else {
            device.pressBack()
        }
    }

    @Test
    fun captureImageButton_successTest() {
        val button = device.findObject(
            By.text(mResources.getString(R.string.capture_image))
                .clazz(TestConst.BUTTON_CLASS_NAME),
        )
        button?.clickAndWait()
        clickPermissionAllow()
        successCameraCapture()
        checkItemAddedOnList(1)
    }

    @Test
    fun captureImageButton_FailTest() {
        val button = device.findObject(
            By.text(mResources.getString(R.string.capture_image))
                .clazz(TestConst.BUTTON_CLASS_NAME),
        )
        button.clickAndWait()
        clickPermissionAllow()
        device.pressBack()
        checkItemAddedOnList(0)
    }

    @Test
    fun captureVideoButton_successTest() {
        val button = device.findObject(
            By.text(mResources.getString(R.string.capture_video))
                .clazz(TestConst.BUTTON_CLASS_NAME),
        )
        button?.clickAndWait()
        clickPermissionAllow()
        successCameraCapture(true)
        checkItemAddedOnList(1)
    }

    @Test
    fun captureVideoButton_FailTest() {
        val button = device.findObject(
            By.text(mResources.getString(R.string.capture_video))
                .clazz(TestConst.BUTTON_CLASS_NAME),
        )
        button?.clickAndWait()
        clickPermissionAllow()
        device.pressBack()
        checkItemAddedOnList(0)
    }
}
