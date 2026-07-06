package com.nareshchocha.filepicker.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.nareshchocha.filepicker.BuildConfig

@Suppress("TooManyFunctions")
class AppOpenAdManager(
    application: Application
) : Application.ActivityLifecycleCallbacks {
    companion object {
        private const val TAG = "AppOpenAdManager"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private var isLoadingAd = false
    private var hasShownAd = false
    private var hasStarted = false
    private var currentActivity: Activity? = null

    init {
        // Register synchronously in Application.onCreate — before any Activity is
        // created — so the very first onActivityResumed is captured. Loading is
        // still gated behind consent via start(); we only track the activity here.
        application.registerActivityLifecycleCallbacks(this)
    }

    fun start() {
        if (hasStarted) return
        hasStarted = true
        loadAd()
    }

    private fun showAdIfAvailable(activity: Activity) {
        if (hasShownAd || isShowingAd) return

        val ad = appOpenAd
        if (ad == null) {
            Log.d(TAG, "App open ad not ready yet")
            return
        }

        ad.adEventCallback =
            object : AppOpenAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App open ad dismissed")
                    appOpenAd = null
                    isShowingAd = false
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    Log.w(TAG, "App open ad failed to show: ${fullScreenContentError.message}")
                    appOpenAd = null
                    isShowingAd = false
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App open ad showed")
                }
            }

        hasShownAd = true
        isShowingAd = true
        ad.show(activity)
    }

    private fun loadAd() {
        if (isLoadingAd || appOpenAd != null) return

        isLoadingAd = true
        AppOpenAd.load(
            AdRequest.Builder(BuildConfig.ADMOB_APP_OPEN_AD_UNIT_ID).build(),
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded")
                    appOpenAd = ad
                    isLoadingAd = false
                    if (!hasShownAd) {
                        currentActivity?.let { showAdIfAvailable(it) }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "App open ad failed to load: ${adError.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    // ActivityLifecycleCallbacks — track current activity
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        // No-op: required by ActivityLifecycleCallbacks
    }

    override fun onActivityStarted(activity: Activity) {
        // No-op: required by ActivityLifecycleCallbacks
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        if (!hasShownAd) {
            showAdIfAvailable(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        // No-op: required by ActivityLifecycleCallbacks
    }

    override fun onActivityStopped(activity: Activity) {
        // No-op: required by ActivityLifecycleCallbacks
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
        // No-op: required by ActivityLifecycleCallbacks
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
