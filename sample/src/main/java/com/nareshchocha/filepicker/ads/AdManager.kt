package com.nareshchocha.filepicker.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.nareshchocha.filepicker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var actionCount = 0
    private const val INTERSTITIAL_FREQUENCY = 4 // show every 5th action
    private val isMobileAdsInitialized = AtomicBoolean(false)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /**
     * Suspends until the Mobile Ads SDK finishes initializing (exactly once).
     * Must be called after user consent has been gathered.
     * The next-gen SDK requires initialization on a background thread.
     */
    suspend fun initialize(context: Context) {
        if (isMobileAdsInitialized.getAndSet(true)) {
            // Already started — wait for the in-progress init to complete before returning.
            _isInitialized.first { it }
            return
        }
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                MobileAds.initialize(
                    context.applicationContext,
                    InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build()
                ) { status ->
                    Log.d(TAG, "MobileAds initialized")
                    status.adapterStatusMap.forEach { (name, adapterStatus) ->
                        Log.d(
                            "Ads",
                            "$name -> ${adapterStatus.initializationState} : ${adapterStatus.description}"
                        )
                    }
                    _isInitialized.value = true
                    continuation.resume(Unit)
                }
            }
        }
    }

    fun loadInterstitialAd() {
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        InterstitialAd.load(
            AdRequest.Builder(BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID).build(),
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${adError.message}")
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
            loadInterstitialAd()
            return
        }

        ad.adEventCallback =
            object : InterstitialAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    actionCount = 0 // Reset counter only when shown successfully!
                    loadInterstitialAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${fullScreenContentError.message}")
                    interstitialAd = null
                    actionCount = 0 // Reset counter on failure to show as well
                    loadInterstitialAd()
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed")
                }
            }

        ad.show(activity)
    }
}
