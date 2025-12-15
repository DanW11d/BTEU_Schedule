package com.example.bteu_schedule.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Используем общий DataStore из SharedDataStore

/**
 * Режим языка интерфейса приложения
 */
enum class LanguageMode {
    RUSSIAN,      // Русский язык
    BELARUSIAN,   // Белорусский язык
    ENGLISH       // Английский язык
}

/**
 * Менеджер языка, управляемый Hilt и сохраняющий состояние в DataStore.
 */
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language_mode")
    }

    // StateFlow для UI - используем stateIn для безопасной подписки на DataStore
    val languageMode: StateFlow<LanguageMode> = context.settingsDataStore.data
        .map { preferences ->
            val languageName = preferences[LANGUAGE_KEY] ?: LanguageMode.RUSSIAN.name
            try {
                LanguageMode.valueOf(languageName)
            } catch (e: IllegalArgumentException) {
                LanguageMode.RUSSIAN
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = LanguageMode.RUSSIAN
        )

    fun updateLanguageMode(mode: LanguageMode) {
        coroutineScope.launch {
            context.settingsDataStore.edit {
                it[LANGUAGE_KEY] = mode.name
            }
        }
    }

    /**
     * Получить текущую локаль на основе выбранного режима
     */
    @Composable
    fun getCurrentLocale(): Locale {
        val currentLanguage by languageMode.collectAsState()
        return when (currentLanguage) {
            LanguageMode.RUSSIAN -> Locale("ru", "RU")
            LanguageMode.BELARUSIAN -> Locale("be", "BY")
            LanguageMode.ENGLISH -> Locale("en", "US")
        }
    }

    /**
     * Получить локаль для применения
     */
    fun getLocaleForContext(context: Context): Locale {
        return when (languageMode.value) {
            LanguageMode.RUSSIAN -> Locale("ru", "RU")
            LanguageMode.BELARUSIAN -> Locale("be", "BY")
            LanguageMode.ENGLISH -> Locale("en", "US")
        }
    }
    
    /**
     * Применить локаль к контексту (для Android 13+ используем более современный подход)
     * ВАЖНО: Этот метод устарел, лучше использовать recreate() Activity
     */
    @Suppress("DEPRECATION")
    fun applyLocale(context: Context) {
        val locale = getLocaleForContext(context)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}


