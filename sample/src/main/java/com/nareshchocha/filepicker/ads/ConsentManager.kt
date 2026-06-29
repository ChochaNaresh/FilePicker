package com.nareshchocha.filepicker.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.nareshchocha.filepicker.BuildConfig

/**
 * Wraps Google's User Messaging Platform (UMP) consent flow.
 *
 * Required for serving ads to users in the European Economic Area (EEA),
 * the United Kingdom and Switzerland. Consent must be gathered before
 * `MobileAds.initialize` and before any ad is requested.
 *
 * The typical flow on every app launch is:
 *  1. [gatherConsent] – requests the latest consent info and shows the
 *     consent form if one is required.
 *  2. Once the callback fires, check [canRequestAds]; if `true` it is safe
 *     to initialize the Mobile Ads SDK and request ads.
 */
object ConsentManager {
    private const val TAG = "ConsentManager"

    /**
     * Replace with your own test device's hashed ID (printed in logcat by the
     * Mobile Ads SDK as `Use ... to get test ads on this device.`) to preview
     * the consent form while developing. Only used in debug builds.
     */
    private const val TEST_DEVICE_HASHED_ID = "33BE2250B43518CCDA7DE426D04EE231"

    /**
     * Requests the latest consent information and shows the consent form if
     * required. [onComplete] is invoked on the main thread once the form is
     * dismissed (or immediately if no form is required), regardless of whether
     * an error occurred — callers should gate ads behind [canRequestAds].
     */
    fun gatherConsent(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        val params =
            ConsentRequestParameters
                .Builder()
                .apply {
                    if (BuildConfig.DEBUG) {
                        setConsentDebugSettings(
                            ConsentDebugSettings
                                .Builder(activity)
                                // Forces the EEA experience on registered test devices only.
                                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                                .addTestDeviceHashedId(TEST_DEVICE_HASHED_ID)
                                .build()
                        )
                    }
                }.build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info is up to date; show the form if it is required.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.errorCode} ${formError.message}")
                    }
                    onComplete()
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.errorCode} ${requestError.message}")
                onComplete()
            }
        )
    }

    /**
     * Whether ads can be requested based on the user's current consent state.
     * Returns `true` when consent has been obtained or is not required.
     */
    fun canRequestAds(activity: Activity): Boolean =
        UserMessagingPlatform.getConsentInformation(activity).canRequestAds()

    /**
     * Resets the consent state. Useful for testing the form repeatedly.
     */
    fun reset(activity: Activity) {
        UserMessagingPlatform.getConsentInformation(activity).reset()
    }
}
