plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    id("kotlin-parcelize")
}

configurations.all {
    resolutionStrategy {
        force("androidx.security:security-crypto:1.1.0")
    }
}

android {
    namespace = "com.example.bteu_schedule"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bteu_schedule"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.bteu_schedule.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            // Оптимизация для debug сборки - уменьшаем использование памяти
            isDebuggable = true
        }
    }
    
    // Настройка App Bundle для уменьшения размера приложения
    bundle {
        language {
            // Отключаем разделение по языкам, все языки в одном пакете
            enableSplit = false
        }
        density {
            // Включаем разделение по плотности экрана для уменьшения размера
            enableSplit = true
        }
        abi {
            // Включаем разделение по ABI для уменьшения размера
            enableSplit = true
        }
    }
    
    // Оптимизация ресурсов
    packaging {
        resources {
            // Исключаем ненужные файлы из APK/AAB
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin"
            excludes += "/kotlin/**"
            excludes += "/kotlinx/**"
            excludes += "**/attach_hotspot_windows.dll"
            excludes += "META-INF/licenses/**"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "DebugProbesKt.bin"
            excludes += "/META-INF/*.version"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    // Оптимизация обработки ресурсов для уменьшения использования памяти
    androidResources {
        // Оптимизация обработки ресурсов
        noCompress += "tflite"
        noCompress += "lite"
        localeFilters.addAll(listOf("ru", "be", "en"))
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation("androidx.compose.material:material-icons-extended")

    // Material Components for XML themes
    implementation("com.google.android.material:material:1.13.0")
    
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    
    // HTML Parser (Jsoup) для парсинга сайта университета
    implementation("org.jsoup:jsoup:1.17.2")
    
    // DataStore for ThemeManager
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Security Crypto for secure key storage
    implementation("androidx.security:security-crypto:1.1.0")
    
    // Image Loading (Coil) для оптимизированной загрузки изображений
    implementation("io.coil-kt.coil3:coil:3.0.0-alpha06")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-alpha06")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Unit Tests
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.1.4")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // Hilt Testing
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    
    // Android Instrumented Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Hilt Android Testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}