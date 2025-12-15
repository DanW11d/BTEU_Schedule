package com.example.bteu_schedule.data.config

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.bteu_schedule.data.config.AppConstants.PREF_FILE_NAME
import com.example.bteu_schedule.data.config.AppConstants.PREF_KEY_CUSTOM_SERVER_IP

object AppConfig {
    const val USE_FALLBACK_DATA = false
    const val SHOW_FALLBACK_WARNING = true
    // Логирование для отладки (отключено для production)
    // Включите true только для отладки
    const val LOG_API_REQUESTS = false
    
    // Детальное логирование FTP и парсинга (отключено для production)
    // Включите true только для отладки проблем с FTP
    const val LOG_FTP_DETAILS = true
    
    // Максимальное количество курсов
    const val MAX_COURSE = 4
    
    // Максимальный размер DBF файла для загрузки в память (50 MB)
    const val MAX_DBF_FILE_SIZE = 50 * 1024 * 1024
    
    // Таймаут для FTP операций (30 секунд)
    const val FTP_TIMEOUT_MS = 30000
    const val BASE_URL = "https://api.bteu-schedule.by/v1/"
    // ВАЖНО: Для release сборки (APK) должно быть false, чтобы использовать продакшн сервер
    // Для debug сборки можно установить true для использования локального сервера
    const val USE_LOCAL_SERVER = false
    const val LOCAL_SERVER_URL_EMULATOR = "http://10.0.2.2:8000/v1/"
    const val LOCAL_SERVER_URL_DEVICE = "http://YOUR_COMPUTER_IP:8000/v1/"
    val LOCAL_SERVER_URL: String
        get() = if (isEmulator()) LOCAL_SERVER_URL_EMULATOR else LOCAL_SERVER_URL_DEVICE

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    fun getLocalServerUrl(context: Context? = null): String {
        context?.let {
            val customIp = getCustomServerIp(it)
            if (customIp.isNotBlank()) {
                return "http://$customIp:8000/v1/"
            }
        }
        return if (isEmulator()) LOCAL_SERVER_URL_EMULATOR else LOCAL_SERVER_URL_DEVICE
    }

    fun setCustomServerIp(context: Context, ip: String) {
        try {
            val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(AppConstants.PREF_KEY_CUSTOM_SERVER_IP, ip.trim()).apply()
        } catch (e: Exception) {
            Log.e("AppConfig", "Ошибка сохранения кастомного IP", e)
        }
    }

    fun getCustomServerIp(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            prefs.getString(AppConstants.PREF_KEY_CUSTOM_SERVER_IP, "") ?: ""
        } catch (e: Exception) {
            Log.e("AppConfig", "Ошибка получения кастомного IP", e)
            ""
        }
    }

    fun clearCustomServerIp(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(AppConstants.PREF_KEY_CUSTOM_SERVER_IP).apply()
        } catch (e: Exception) {
            Log.e("AppConfig", "Ошибка очистки кастомного IP", e)
        }
    }
}