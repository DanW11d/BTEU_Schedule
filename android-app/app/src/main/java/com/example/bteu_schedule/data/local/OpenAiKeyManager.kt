package com.example.bteu_schedule.data.local

import android.content.Context
import com.example.bteu_schedule.utils.AppLogger
import com.example.bteu_schedule.utils.SecureKeyStorage

/**
 * Менеджер для безопасного хранения OpenAI API ключа
 * 
 * Использует Android Keystore через SecureKeyStorage для защиты API ключей.
 * Данные автоматически шифруются при сохранении.
 */
object OpenAiKeyManager {
    
    // Константы для хранения
    private const val SECURE_KEY_API_KEY = "openai_api_key"
    private const val PREF_NAME_OLD = "openai_prefs" // Старое имя для миграции
    private const val KEY_API_KEY_OLD = "api_key" // Старый ключ для миграции
    
    // Флаг миграции
    private var migrationAttempted = false
    
    /**
     * Выполнить миграцию ключа из старого хранилища (SharedPreferences) в новое безопасное
     * Вызывается автоматически при первом обращении
     */
    private fun migrateIfNeeded(context: Context) {
        if (migrationAttempted) return
        
        migrationAttempted = true
        SecureKeyStorage.migrateFromSharedPreferences(
            context = context,
            oldPrefsName = PREF_NAME_OLD,
            oldKey = KEY_API_KEY_OLD,
            newKey = SECURE_KEY_API_KEY
        )
    }
    
    /**
     * Сохранить API ключ в безопасное хранилище
     * 
     * @param context Контекст приложения
     * @param apiKey API ключ для сохранения (будет зашифрован)
     */
    fun saveApiKey(context: Context, apiKey: String) {
        try {
            val success = SecureKeyStorage.saveSecureValue(
                context = context,
                key = SECURE_KEY_API_KEY,
                value = apiKey
            )
            
            if (success) {
                AppLogger.d("OpenAiKeyManager", "API ключ сохранен в безопасное хранилище")
            } else {
                AppLogger.e("OpenAiKeyManager", "Не удалось сохранить API ключ в безопасное хранилище")
            }
        } catch (e: Exception) {
            AppLogger.e("OpenAiKeyManager", "Ошибка сохранения API ключа", e)
        }
    }
    
    /**
     * Получить API ключ из безопасного хранилища
     * 
     * @param context Контекст приложения
     * @return API ключ или null, если не настроен
     */
    fun getApiKey(context: Context): String? {
        return try {
            // Выполняем миграцию при первом обращении
            migrateIfNeeded(context)
            
            val key = SecureKeyStorage.getSecureValue(context, SECURE_KEY_API_KEY)
            if (key.isNullOrBlank()) {
                null
            } else {
                key
            }
        } catch (e: Exception) {
            AppLogger.e("OpenAiKeyManager", "Ошибка получения API ключа", e)
            null
        }
    }
    
    /**
     * Проверить, настроен ли API ключ
     * 
     * @param context Контекст приложения
     * @return true, если API ключ настроен
     */
    fun isApiKeyConfigured(context: Context): Boolean {
        return try {
            migrateIfNeeded(context)
            SecureKeyStorage.hasSecureValue(context, SECURE_KEY_API_KEY)
        } catch (e: Exception) {
            AppLogger.e("OpenAiKeyManager", "Ошибка проверки наличия API ключа", e)
            false
        }
    }
    
    /**
     * Удалить API ключ из безопасного хранилища
     * 
     * @param context Контекст приложения
     */
    fun clearApiKey(context: Context) {
        try {
            val success = SecureKeyStorage.removeSecureValue(context, SECURE_KEY_API_KEY)
            
            if (success) {
                AppLogger.d("OpenAiKeyManager", "API ключ удален из безопасного хранилища")
            } else {
                AppLogger.w("OpenAiKeyManager", "Не удалось удалить API ключ из безопасного хранилища")
            }
        } catch (e: Exception) {
            AppLogger.e("OpenAiKeyManager", "Ошибка удаления API ключа", e)
        }
    }
}

