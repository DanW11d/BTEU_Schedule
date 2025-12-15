package com.example.bteu_schedule.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
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
import javax.inject.Inject
import javax.inject.Singleton

// Используем общий DataStore из SharedDataStore

/**
 * Режим темы приложения
 */
enum class ThemeMode {
    LIGHT,      // Светлая тема
    DARK,       // Темная тема
    SYSTEM      // Следовать системной теме
}

/**
 * Менеджер темы, управляемый Hilt и сохраняющий состояние в DataStore.
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    // StateFlow для UI - используем stateIn для безопасной подписки на DataStore
    val themeMode: StateFlow<ThemeMode> = context.settingsDataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun updateThemeMode(mode: ThemeMode) {
        coroutineScope.launch {
            context.settingsDataStore.edit {
                it[THEME_KEY] = mode.name
            }
        }
    }

    @Composable
    fun isDarkTheme(): Boolean {
        val currentTheme by themeMode.collectAsState()
        return when (currentTheme) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}

// LocalThemeManager и ProvideThemeManager больше не нужны, так как Hilt управляет зависимостью.
