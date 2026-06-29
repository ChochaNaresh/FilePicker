package com.nareshchocha.filepicker.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.nareshchocha.filepicker.BuildConfig

@Suppress("TooManyFunctions")
class AppOpenAdManager(
    private val application: Application
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

    fun start() {
        if (hasStarted) return
        hasStarted = true
        application.registerActivityLifecycleCallbacks(this)
        loadAd()
    }

    /**
     * Called from [onActivityResumed] to show the ad once on first launch.
     */
    private fun showAdIfAvailable(activity: Activity) {
        if (hasShownAd || isShowingAd) return

        val ad = appOpenAd
        if (ad == null) {
            Log.d(TAG, "App open ad not ready yet")
            return
        }

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App open ad dismissed")
                    appOpenAd = null
                    isShowingAd = false
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    Log.w(TAG, "App open ad failed to show: ${error.message}")
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
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            BuildConfig.ADMOB_APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App open ad loaded")
                    appOpenAd = ad
                    isLoadingAd = false
                    // If an activity is already resumed, show immediately
                    if (!hasShownAd) {
                        currentActivity?.let { showAdIfAvailable(it) }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "App open ad failed to load: ${error.message}")
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
