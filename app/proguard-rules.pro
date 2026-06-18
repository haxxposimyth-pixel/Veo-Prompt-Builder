# Retrofit Proguard Keep Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations

# Moshi rules - keep model descriptors
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-dontwarn com.squareup.moshi.**

# Prevent obfuscating Api model fields and properties
-keepclassmembers class com.example.data.remote.** { *; }
-keepclassmembers class com.example.data.local.** { *; }

# Room databases should keep entity schemas
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.**

