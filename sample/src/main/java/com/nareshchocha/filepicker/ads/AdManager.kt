package com.nareshchocha.filepicker.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nareshchocha.filepicker.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var actionCount = 0
    private const val INTERSTITIAL_FREQUENCY = 5 // show every 5th action
    private val isMobileAdsInitialized = AtomicBoolean(false)

    /**
     * Initializes the Mobile Ads SDK exactly once. Must only be called after
     * user consent has been gathered (see [ConsentManager]).
     */
    fun initialize(context: Context) {
        if (isMobileAdsInitialized.getAndSet(true)) return
        MobileAds.initialize(context) {
            Log.d(TAG, "MobileAds initialized")
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {}
    ) {
        actionCount++
        if (actionCount < INTERSTITIAL_FREQUENCY) {
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(TAG, "Interstitial ad not ready, skipping but keeping counter at threshold ($actionCount)")
            onAdDismissed()
            loadInterstitialAd(activity)
            return
        }

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    actionCount = 0 // Reset counter only when shown successfully!
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${error.message}")
                    interstitialAd = null
                    actionCount = 0 // Reset counter on failure to show as well
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed")
                }
            }

        ad.show(activity)
    }
}
