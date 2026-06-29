-keep,allowobfuscation class com.nareshchocha.filepicker.** {*;}

# Keep WorkManager and Room Database classes (used internally by AdMob SDK)
-keep class androidx.work.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.sqlite.db.SupportSQLiteOpenHelper$Callback { *; }