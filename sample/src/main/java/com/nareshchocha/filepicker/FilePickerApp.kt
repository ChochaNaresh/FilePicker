package com.nareshchocha.filepicker

import android.app.Application
import com.nareshchocha.filepicker.ads.AdManager
import com.nareshchocha.filepicker.ads.AppOpenAdManager
import com.nareshchocha.filepicker.ads.ConsentPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FilePickerApp : Application() {
    /**
     * Created up front but only started once ads may be requested — either from
     * the persisted consent flag on cold start (below) or after the Activity's
     * UMP flow completes (see [com.nareshchocha.filepicker.ads.ConsentManager]).
     */
    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager(this)
        appScope.launch {
            if (ConsentPreferences.isAdsReady(this@FilePickerApp)) {
                AdManager.initialize(this@FilePickerApp)
                AdManager.loadInterstitialAd(this@FilePickerApp)
                appOpenAdManager.start()
            }
        }
    }
}
