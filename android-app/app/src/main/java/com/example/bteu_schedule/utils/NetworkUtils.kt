package com.example.bteu_schedule.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.Log

/**
 * Утилита для проверки подключения к интернету
 */
object NetworkUtils {
    
    private const val TAG = "NetworkUtils"
    
    /**
     * Проверяет, есть ли подключение к интернету
     * @param context Контекст приложения
     * @param requireValidated Если true, требует проверенное соединение (более строгая проверка)
     * @return true, если есть подключение к интернету
     * 
     * ВАЖНО: Эта проверка менее строгая - проверяет только наличие активной сети,
     * а не реальную доступность интернета. Это позволяет приложению попытаться
     * подключиться к серверу, даже если сеть еще не полностью настроена.
     */
    fun isConnected(context: Context, requireValidated: Boolean = false): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Для Android 6.0 (API 23) и выше
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                
                // Упрощенная проверка: если есть активная сеть (WiFi, мобильная, Ethernet) - считаем, что есть подключение
                // Не проверяем NET_CAPABILITY_INTERNET и NET_CAPABILITY_VALIDATED, чтобы избежать ложных отрицаний
                val hasNetwork = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                
                // Если требуется строгая проверка - проверяем валидацию
                if (requireValidated && hasNetwork) {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                } else {
                    hasNetwork
                }
            } else {
                // Для старых версий Android
                @Suppress("DEPRECATION")
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected == true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка проверки подключения: ${e.message}", e)
            // При ошибке считаем, что подключения нет (безопаснее для офлайн-режима)
            false
        }
    }
    
    /**
     * Проверяет, подключен ли Wi-Fi
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }
    
    /**
     * Проверяет, подключена ли мобильная сеть
     */
    fun isMobileConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
        }
    }
    
    /**
     * Логирует информацию о подключении
     */
    fun logConnectionInfo(context: Context) {
        val isConnected = isConnected(context)
        val isWifi = isWifiConnected(context)
        val isMobile = isMobileConnected(context)
        
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Информация о подключении к интернету:")
        Log.d(TAG, "Подключен: $isConnected")
        Log.d(TAG, "Wi-Fi: $isWifi")
        Log.d(TAG, "Мобильная сеть: $isMobile")
        Log.d(TAG, "═══════════════════════════════════════")
    }
}

