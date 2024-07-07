# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontskipnonpubliclibraryclasses
-forceprocessing
-optimizationpasses 5

## disabling Log in release version
-keep class * extends android.app.Activity
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

#-keepclassmembers,allowobfuscation class * {
#  @com.google.gson.annotations.SerializedName <fields>;
# }

## -------------Begin: Retrofit2 ---
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
        @retrofit.http.* <methods>;
}

## -------------End: Retrofit2 ---

##--- Begin:GSON ----
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)

# keep enum so gson can deserialize it
-keepclassmembers enum * { *; }

##--- End:GSON ----

-dontwarn org.greenrobot.greendao.**

-keepattributes *Annotation*
-keep class **$Propertiesbuild

# Only required if you use AsyncExecutor
#-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
#    <init>(java.lang.Throwable);
#}

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes Annotation

-dontwarn freemarker.**

-dontwarn com.google.android.material.*
-keepclasseswithmembers class * {
    @android.material.* <methods>;
}
-dontwarn android.view.accessibility.AccessibilityManager

-dontwarn com.google.errorprone.annotations.*
-dontwarn com.pharmeasy.barcode.*
-dontwarn android.device.ScanManager

-dontwarn dagger.android.ReleaseReferencesAt
-dontwarn dagger.android.AndroidMemorySensitiveReferenceManager

-dontwarn dagger.android.AndroidMemorySensitiveReferenceManager
-dontwarn dagger.android.AndroidMemorySensitiveReferenceManager_Factory

-keep class com.goflash.dispatch.data.** { *; }
-keep class com.goflash.dispatch.model.** { *; }
-keep class com.goflash.dispatch.type.** { *; }

-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

-dontnote rx.internal.util.PlatformDependent

-dontwarn module-info
