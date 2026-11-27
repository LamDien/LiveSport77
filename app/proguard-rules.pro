# Proguard & R8 rules for LiveSport77 Android TV

# Keep Compose for TV runtime & models
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.Immutable *;
    @androidx.compose.runtime.Stable *;
}

# Keep LiveSport Data Models for JSON/StateFlow serialization
-keep class com.livesport77.tv.model.** { *; }

# Coil image loader rules
-keep class coil.** { *; }
-dontwarn coil.**

# Strip verbose/debug/info logs in release builds to avoid leaking internal URLs or state
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

