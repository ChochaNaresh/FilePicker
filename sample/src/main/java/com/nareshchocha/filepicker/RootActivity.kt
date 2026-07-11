package com.nareshchocha.filepicker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.nareshchocha.filepicker.ads.AdManager
import com.nareshchocha.filepicker.ads.BannerAd
import com.nareshchocha.filepicker.ads.ConsentManager
import com.nareshchocha.filepicker.ads.ConsentPreferences
import com.nareshchocha.filepicker.components.AllFilePickers
import com.nareshchocha.filepicker.ui.theme.FilePickerTheme
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import kotlinx.coroutines.launch

class RootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            enableEdgeToEdge()
        }
        // Light (white) status-bar icons to contrast the primary-colored TopAppBar. Uses
        // the non-deprecated insets controller, so it applies on every API level, 15+ included.
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        FilePickerResultContracts.isLoggingEnabled = true
        lifecycleScope.launch {
            // Early-start: if consent was already granted, begin loading ads immediately
            // without blocking on the UMP network round-trip.
            if (ConsentPreferences.isAdsReady(this@RootActivity)) {
                initializeAdsAfterConsent()
            }
            // Always refresh consent on every launch so the UMP SDK can detect
            // revocations, expiry, or new requirements (Google UMP best practice).
            ConsentManager.gatherConsent(this@RootActivity) {
                if (ConsentManager.canRequestAds(this@RootActivity)) {
                    // Safe to call again — AdManager and AppOpenAdManager both guard against
                    // double-init so a second call is a fast no-op.
                    lifecycleScope.launch { initializeAdsAfterConsent() }
                }
            }
        }
        setContent {
            FilePickerTheme {
                RootUI()
            }
        }
    }

    private suspend fun initializeAdsAfterConsent() {
        AdManager.initialize(applicationContext)
        AdManager.loadInterstitialAd()
        (application as? FilePickerApp)?.appOpenAdManager?.start()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootUI() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_sample),
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
        ) {
            AllFilePickers(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            )
            BannerAd()
        }
    }
}
