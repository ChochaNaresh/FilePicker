package com.nareshchocha.filepicker.ads

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** App-level DataStore that survives the [ConsentManager] object being recreated. */
private val Context.consentDataStore: DataStore<Preferences> by preferencesDataStore(name = "consent")

/**
 * Persists whether user consent has already been gathered and ads may be
 * requested, so [com.nareshchocha.filepicker.FilePickerApp] can initialize ads
 * on a cold start without waiting for the Activity's UMP round-trip.
 *
 * The value mirrors [ConsentManager.canRequestAds]; the UMP SDK itself is the
 * source of truth, this is just a fast, synchronously-readable cache.
 */
object ConsentPreferences {
    private val ADS_READY = booleanPreferencesKey("ads_ready")

    // Fire-and-forget writes don't need to block the caller.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Reads the persisted flag once; `false` if never written. */
    suspend fun isAdsReady(context: Context): Boolean =
        context.consentDataStore.data
            .map { it[ADS_READY] ?: false }
            .first()

    /** Persists [ready] off the main thread. Safe to call repeatedly. */
    fun setAdsReady(
        context: Context,
        ready: Boolean
    ) {
        scope.launch {
            context.consentDataStore.edit { it[ADS_READY] = ready }
        }
    }
}
