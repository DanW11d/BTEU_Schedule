# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# Общие правила
# ============================================================================

# Сохраняем информацию о номерах строк для stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Сохраняем аннотации
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ============================================================================
# Kotlin
# ============================================================================

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Parcelize
-keep interface kotlinx.parcelize.Parcelize
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ============================================================================
# Jetpack Compose
# ============================================================================

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.runtime.savedinstancestate.** { *; }
-keep class androidx.compose.runtime.saveable.** { *; }

# Compose UI
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.ui.graphics.** { *; }
-keep class androidx.compose.ui.text.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }

# Compose Material Icons
-keep class androidx.compose.material.icons.** { *; }

# ============================================================================
# Retrofit + OkHttp + Gson
# ============================================================================

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Retrofit Gson Converter
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson - сохраняем модели данных
-keep class com.example.bteu_schedule.data.remote.dto.** { *; }
-keep class * extends com.example.bteu_schedule.data.remote.dto.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ============================================================================
# Room Database
# ============================================================================

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Room Entities и DAOs
-keep class com.example.bteu_schedule.data.local.database.entity.** { *; }
-keep interface com.example.bteu_schedule.data.local.database.dao.** { *; }
-keep class com.example.bteu_schedule.data.local.database.dao.** { *; }
-keep class com.example.bteu_schedule.data.local.database.ScheduleDatabase { *; }

# ============================================================================
# Hilt (Dagger)
# ============================================================================

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Сохраняем Application класс
-keep class com.example.bteu_schedule.BTEUApplication { *; }

# Сохраняем DI модули
-keep class com.example.bteu_schedule.di.** { *; }

# ============================================================================
# DataStore
# ============================================================================

-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ============================================================================
# Apache Commons Net (FTP)
# ============================================================================

-keep class org.apache.commons.net.** { *; }
-dontwarn org.apache.commons.net.**

# ============================================================================
# ViewModels
# ============================================================================

-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.example.bteu_schedule.ui.viewmodel.** { *; }
-keep class com.example.bteu_schedule.viewmodel.** { *; }

# ============================================================================
# Repositories
# ============================================================================

-keep class com.example.bteu_schedule.data.repository.** { *; }

# ============================================================================
# Domain Models
# ============================================================================

-keep class com.example.bteu_schedule.domain.models.** { *; }

# ============================================================================
# Общие классы приложения
# ============================================================================

# Сохраняем Application класс
-keep class com.example.bteu_schedule.BTEUApplication { *; }

# Сохраняем MainActivity
-keep class com.example.bteu_schedule.MainActivity { *; }

# Сохраняем Config
-keep class com.example.bteu_schedule.data.config.AppConfig { *; }

# Сохраняем Theme Manager
-keep class com.example.bteu_schedule.ui.theme.** { *; }

# ============================================================================
# Сериализация и Reflection
# ============================================================================

# Для сохранения работы reflection в Gson и Room
-keepclassmembers class * {
    @androidx.room.ColumnInfo *;
}

# Сохраняем enum значения
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================================================
# R классы (Resources)
# ============================================================================

-keepclassmembers class **.R$* {
    public static <fields>;
}

# ============================================================================
# Kotlinx Coroutines для работы с Flow
# ============================================================================

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ============================================================================
# Lifecycle
# ============================================================================

-keep class androidx.lifecycle.** { *; }

# ============================================================================
# Coil (Image Loading)
# ============================================================================

-keep class coil3.** { *; }
-dontwarn coil3.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ============================================================================
# Security Crypto (EncryptedSharedPreferences)
# ============================================================================

-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ============================================================================
# Обработка предупреждений
# ============================================================================

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn kotlin.reflect.**
-dontwarn kotlin.Unit
-dontwarn kotlin.jvm.internal.**
-dontwarn org.jetbrains.annotations.**

# ============================================================================
# Удаление неиспользуемого кода (опционально)
# ============================================================================

# Раскомментируйте для более агрессивной оптимизации:
# -optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# -optimizationpasses 5
# -allowaccessmodification
# -repackageclasses ''
