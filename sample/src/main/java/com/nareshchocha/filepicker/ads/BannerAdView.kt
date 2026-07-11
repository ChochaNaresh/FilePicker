package com.nareshchocha.filepicker.ads

import android.app.Activity
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.nareshchocha.filepicker.BuildConfig
import com.nareshchocha.filepicker.ads.AdManager
import kotlinx.coroutines.flow.first

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adUnitId: String = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    val activity = context as Activity
    val density = LocalDensity.current
    val screenWidthDp =
        with(density) {
            LocalWindowInfo.current.containerSize.width
                .toDp()
                .value
                .toInt()
        }

    var isLoaded by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "bannerFade"
    )

    val adView = remember(adUnitId) { AdView(activity) }

    LaunchedEffect(adUnitId, screenWidthDp) {
        AdManager.isInitialized.first { it }
        isLoaded = false
        val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
        adView.loadAd(
            BannerAdRequest.Builder(adUnitId, adSize).build(),
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    isLoaded = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w("AdMobBanner", "AdMob banner failed to load: ${adError.message}")
                }
            }
        )
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    AndroidView(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .alpha(alpha),
        factory = { adView }
    )
}
