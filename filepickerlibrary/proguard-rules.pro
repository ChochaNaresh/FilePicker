# Keep public API models: class names must not be obfuscated (referenced via
# ::class.java.name for picker result routing) and all members must survive for
# Parcelable serialisation.
-keep class com.nareshchocha.filepickerlibrary.models.** { *; }
