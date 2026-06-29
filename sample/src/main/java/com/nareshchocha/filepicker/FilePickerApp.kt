package com.nareshchocha.filepicker

import android.app.Application
import com.nareshchocha.filepicker.ads.AppOpenAdManager

class FilePickerApp : Application() {
    /**
     * Created up front but only started after user consent is gathered
     * (see [com.nareshchocha.filepicker.ads.ConsentManager] and RootActivity).
     */
    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager = AppOpenAdManager(this)
        // MobileAds initialization and ad loading are deferred until the
        // consent flow completes and canRequestAds() returns true.
    }
}
