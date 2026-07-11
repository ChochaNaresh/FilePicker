package com.nareshchocha.filepicker

import android.app.Application
import com.nareshchocha.filepicker.ads.AppOpenAdManager

class FilePickerApp : Application() {
    /**
     * Created up front but only started once ads may be requested — either from
     * the persisted consent flag on cold start or after the Activity's UMP flow
     * completes (see [com.nareshchocha.filepicker.ads.ConsentManager]).
     * RootActivity owns the full init sequence (initialize → loadInterstitial → start).
     */
    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager(this)
    }
}
