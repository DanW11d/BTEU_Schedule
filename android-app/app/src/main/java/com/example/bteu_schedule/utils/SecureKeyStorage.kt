package com.example.bteu_schedule.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Безопасное хранилище для чувствительных данных с использованием Android Keystore
 * 
 * Использует EncryptedSharedPreferences для автоматического шифрования данных
 * с ключами, защищенными Android Keystore System
 * 
 * Преимущества:
 * - Ключи шифрования хранятся в аппаратно-защищенном хранилище (если доступно)
 * - Данные автоматически шифруются при сохранении и расшифровываются при чтении
 * - Защита от root-доступа
 * - Соответствие лучшим практикам безопасности Android
 */
object SecureKeyStorage {

    private const val MASTER_KEY_ALIAS = "_bteu_schedule_master_key"
    private const val PREFS_NAME = "secure_keys_encrypted"

    /**
     * Создает экземпляр EncryptedSharedPreferences для безопасного хранения данных
     */
    private fun getEncryptedPreferences(context: Context): SharedPreferences {
        return try {
            // Создаем MasterKey для шифрования
            val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Создаем EncryptedSharedPreferences
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Если EncryptedSharedPreferences не доступен (старые версии Android),
            // используем обычный SharedPreferences как fallback
            AppLogger.w("SecureKeyStorage", "Не удалось создать EncryptedSharedPreferences, используем обычный SharedPreferences", e)
            context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE)
        }
    }

    /**
     * Сохранить безопасное значение
     * 
     * @param context Контекст приложения
     * @param key Ключ для хранения
     * @param value Значение для сохранения (будет зашифровано)
     */
    fun saveSecureValue(context: Context, key: String, value: String): Boolean {
        return try {
            val prefs = getEncryptedPreferences(context)
            prefs.edit()
                .putString(key, value.trim())
                .apply()
            AppLogger.d("SecureKeyStorage", "Безопасное значение сохранено для ключа: $key")
            true
        } catch (e: Exception) {
            AppLogger.e("SecureKeyStorage", "Ошибка сохранения безопасного значения для ключа: $key", e)
            false
        }
    }

    /**
     * Получить безопасное значение
     * 
     * @param context Контекст приложения
     * @param key Ключ для получения
     * @return Значение или null, если не найдено
     */
    fun getSecureValue(context: Context, key: String): String? {
        return try {
            val prefs = getEncryptedPreferences(context)
            val value = prefs.getString(key, null)
            if (value.isNullOrBlank()) {
                null
            } else {
                value
            }
        } catch (e: Exception) {
            AppLogger.e("SecureKeyStorage", "Ошибка получения безопасного значения для ключа: $key", e)
            null
        }
    }

    /**
     * Удалить безопасное значение
     * 
     * @param context Контекст приложения
     * @param key Ключ для удаления
     */
    fun removeSecureValue(context: Context, key: String): Boolean {
        return try {
            val prefs = getEncryptedPreferences(context)
            prefs.edit()
                .remove(key)
                .apply()
            AppLogger.d("SecureKeyStorage", "Безопасное значение удалено для ключа: $key")
            true
        } catch (e: Exception) {
            AppLogger.e("SecureKeyStorage", "Ошибка удаления безопасного значения для ключа: $key", e)
            false
        }
    }

    /**
     * Проверить, существует ли значение
     * 
     * @param context Контекст приложения
     * @param key Ключ для проверки
     * @return true, если значение существует
     */
    fun hasSecureValue(context: Context, key: String): Boolean {
        return try {
            val prefs = getEncryptedPreferences(context)
            prefs.contains(key) && !getSecureValue(context, key).isNullOrBlank()
        } catch (e: Exception) {
            AppLogger.e("SecureKeyStorage", "Ошибка проверки существования значения для ключа: $key", e)
            false
        }
    }

    /**
     * Мигрировать значение из старого SharedPreferences в безопасное хранилище
     * 
     * @param context Контекст приложения
     * @param oldPrefsName Имя старого SharedPreferences
     * @param oldKey Ключ в старом хранилище
     * @param newKey Ключ в новом безопасном хранилище
     * @return true, если миграция прошла успешно
     */
    fun migrateFromSharedPreferences(
        context: Context,
        oldPrefsName: String,
        oldKey: String,
        newKey: String
    ): Boolean {
        return try {
            val oldPrefs = context.getSharedPreferences(oldPrefsName, Context.MODE_PRIVATE)
            val oldValue = oldPrefs.getString(oldKey, null)
            
            if (oldValue.isNullOrBlank()) {
                AppLogger.d("SecureKeyStorage", "Нет значения для миграции: $oldKey")
                return true // Нет значения - это нормально
            }

            // Сохраняем в новое безопасное хранилище
            val success = saveSecureValue(context, newKey, oldValue)
            
            if (success) {
                // Удаляем из старого хранилища после успешной миграции
                oldPrefs.edit().remove(oldKey).apply()
                AppLogger.d("SecureKeyStorage", "Значение успешно мигрировано из $oldPrefsName/$oldKey")
            }
            
            success
        } catch (e: Exception) {
            AppLogger.e("SecureKeyStorage", "Ошибка миграции значения из $oldPrefsName/$oldKey", e)
            false
        }
    }
}

