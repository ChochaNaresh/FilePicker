package com.nareshchocha.filepicker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import org.hamcrest.Matchers

private val denyStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    "DENY"
} else {
    "Deny"
}
private val allowStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        "Allow"
    } else {
        "ALLOW"
    }
} else {
    "Allow"
}

internal fun UiDevice.clickAllAllowOnlyThisTimeButtons() {
    val allowButton = findObject(
        By.text("While using the app").clazz(TestConst.BUTTON_CLASS_NAME),
    )
    allowButton?.clickAndWait()
    if (allowButton != null) {
        clickAllAllowOnlyThisTimeButtons()
    }
}

internal fun UiDevice.clickAllAllowLocationButtons() {
    val allowButton = findObject(
        By.text("Allow all the time").clazz(TestConst.BUTTON_CLASS_NAME),
    )
    allowButton?.clickAndWait()
    if (allowButton != null) {
        clickAllAllowLocationButtons()
    }
}

fun UiDevice.clickAllDenyButtons(withDonAsk: Boolean = false) {
    val denyButton = findObject(
        By.text(denyStr).clazz(TestConst.BUTTON_CLASS_NAME),
    )
    if (withDonAsk && denyButton != null) {
        val dontButton = findObject(
            By.clazz(TestConst.CHECK_BOX_CLASS_NAME),
        )
        dontButton.clickAndWait()
        val allowButton = findObject(
            By.text(allowStr).clazz(TestConst.BUTTON_CLASS_NAME),
        )
        TestCase.assertNotNull(allowButton)
        TestCase.assertEquals(allowButton.isEnabled, false)
    }

    denyButton?.clickAndWait()
    if (denyButton != null) {
        clickAllDenyButtons(withDonAsk)
    }
}

// Start from the home screen
fun UiDevice.startAppAndWaitLaunch() {
    pressHome()

    // Wait for launcher
    val launcherPackage: String = launcherPackageName
    ViewMatchers.assertThat(launcherPackage, Matchers.notNullValue())
    wait(
        Until.hasObject(By.pkg(launcherPackage).depth(0)),
        TestConst.LAUNCH_TIMEOUT,
    )
    // Launch the app
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = context.packageManager.getLaunchIntentForPackage(
        TestConst.BASIC_SAMPLE_PACKAGE,
    )?.apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(intent)
    wait(
        Until.hasObject(By.pkg(TestConst.BASIC_SAMPLE_PACKAGE).depth(0)),
        TestConst.LAUNCH_TIMEOUT,
    )
}

fun UiDevice.clickAllAllowButtons() {
    val allowButton = findObject(
        By.text(allowStr).clazz(TestConst.BUTTON_CLASS_NAME),
    )
    allowButton?.clickAndWait()
    if (allowButton != null) {
        clickAllAllowButtons()
    }
}

fun UiObject2.clickAndWait() {
    this.clickAndWait(
        Until.newWindow(),
        TestConst.BUTTON_CLICK_TIMEOUT,
    )
}
