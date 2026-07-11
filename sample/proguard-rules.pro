# ── App package ──────────────────────────────────────────────────────────────
# Keep all sample app classes; names must be stable for the AndroidManifest
# component references (Application, Activity) and for any reflection paths.
-keep class com.nareshchocha.filepicker.** { *; }

# ── Google Mobile Ads SDK (next-gen: ads-mobile-sdk) ─────────────────────────
# The SDK ships consumer rules, but mediation adapter discovery uses reflection
# to instantiate adapter classes by name at runtime — keep those names.
-keep class com.google.android.libraries.ads.** { *; }
-dontwarn com.google.android.libraries.ads.**

# ── User Messaging Platform (UMP / GDPR consent) ─────────────────────────────
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# ── Liftoff Monetize (Vungle) mediation adapter ───────────────────────────────
# The adapter is resolved by the AdMob mediation layer via class-name lookup.
-keep class com.vungle.** { *; }
-keep interface com.vungle.** { *; }
-dontwarn com.vungle.**
-keep class com.google.ads.mediation.vungle.** { *; }
-dontwarn com.google.ads.mediation.vungle.**

# ── AndroidX WorkManager and Room ─────────────────────────────────────────────
# Used internally by the AdMob SDK's mediation pipeline and offline caching.
-keep class androidx.work.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.sqlite.db.SupportSQLiteOpenHelper$Callback { *; }

# ── Kotlin Coroutines (R8 full-mode compatibility) ───────────────────────────
# R8 full mode can remove volatile field intrinsics that coroutines rely on.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
