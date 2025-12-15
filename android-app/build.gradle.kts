// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    
    // Плагин для проверки обновлений зависимостей
    id("com.github.ben-manes.versions") version "0.51.0" apply true
}

// Конфигурация плагина dependencyUpdates
tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
    checkForGradleUpdate = true
    outputFormatter = "html"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
    
    // Фильтр для исключения alpha, beta, rc версий
    rejectVersionIf {
        candidate.version.isNonStable()
    }
}

/**
 * Проверка, является ли версия нестабильной (alpha, beta, rc, etc.)
 */
fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { 
        uppercase(java.util.Locale.getDefault()).contains(it) 
    }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}
