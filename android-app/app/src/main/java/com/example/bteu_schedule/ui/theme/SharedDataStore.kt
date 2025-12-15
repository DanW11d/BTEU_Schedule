package com.example.bteu_schedule.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Общий DataStore для всех настроек приложения (тема, язык и т.д.)
 * Используется как singleton, чтобы избежать конфликтов "multiple DataStores active"
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


