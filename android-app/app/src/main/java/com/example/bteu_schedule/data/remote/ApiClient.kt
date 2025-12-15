package com.example.bteu_schedule.data.remote

import android.content.Context
import android.util.Log
import com.example.bteu_schedule.data.config.AppConfig
import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Конфигурация API клиента
 */
object ApiClient {
    
    // Контекст для определения типа устройства (эмулятор/реальное устройство)
    private var appContext: Context? = null
    
    /**
     * Инициализировать контекст приложения (вызывать в MainActivity.onCreate)
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("ApiClient", "Контекст инициализирован")
    }
    
    /**
     * Базовый URL API
     * Используется URL из AppConfig, можно переключить на локальный сервер
     * Автоматически определяет тип устройства (эмулятор/реальное устройство)
     */
    private val BASE_URL: String by lazy {
        try {
            if (AppConfig.USE_LOCAL_SERVER) {
                // Автоматически определяем URL в зависимости от типа устройства
                // Если контекст еще не установлен, используем определение по isEmulator()
                val url = if (appContext != null) {
                    AppConfig.getLocalServerUrl(appContext)
                } else {
                    // Определяем URL без контекста (для обратной совместимости)
                    if (AppConfig.isEmulator()) {
                        AppConfig.LOCAL_SERVER_URL_EMULATOR
                    } else {
                        AppConfig.LOCAL_SERVER_URL_DEVICE
                    }
                }
                Log.d("ApiClient", "Определен локальный URL: $url")
                url
            } else {
                AppConfig.BASE_URL
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "Ошибка получения BASE_URL", e)
            // Если USE_LOCAL_SERVER = true, используем локальный URL даже при ошибке
            if (AppConfig.USE_LOCAL_SERVER) {
                val fallbackUrl = if (AppConfig.isEmulator()) {
                    AppConfig.LOCAL_SERVER_URL_EMULATOR
                } else {
                    AppConfig.LOCAL_SERVER_URL_DEVICE
                }
                Log.w("ApiClient", "Используется fallback локальный URL: $fallbackUrl")
                fallbackUrl
            } else {
                // Только если USE_LOCAL_SERVER = false, используем продакшн
                Log.w("ApiClient", "Используется fallback продакшн URL")
                "https://api.bteu-schedule.by/v1/"
            }
        }
    }
    
    init {
        try {
            Log.d("ApiClient", "═══════════════════════════════════════")
            Log.d("ApiClient", "Инициализация ApiClient")
            Log.d("ApiClient", "USE_LOCAL_SERVER: ${AppConfig.USE_LOCAL_SERVER}")
            Log.d("ApiClient", "Тип устройства: ${if (AppConfig.isEmulator()) "Эмулятор" else "Реальное устройство"}")
            Log.d("ApiClient", "Контекст установлен: ${appContext != null}")
            
            // Вычисляем финальный URL (будет использован при первом обращении к BASE_URL)
            val finalUrl = if (AppConfig.USE_LOCAL_SERVER) {
                if (appContext != null) {
                    AppConfig.getLocalServerUrl(appContext)
                } else {
                    // Если контекст еще не установлен, используем определение по типу устройства
                    if (AppConfig.isEmulator()) {
                        AppConfig.LOCAL_SERVER_URL_EMULATOR
                    } else {
                        AppConfig.LOCAL_SERVER_URL_DEVICE
                    }
                }
            } else {
                AppConfig.BASE_URL
            }
            
            if (AppConfig.USE_LOCAL_SERVER) {
                Log.w("ApiClient", "⚠️ Используется ЛОКАЛЬНЫЙ сервер для разработки!")
                if (AppConfig.isEmulator()) {
                    Log.d("ApiClient", "URL для эмулятора: ${AppConfig.LOCAL_SERVER_URL_EMULATOR}")
                } else {
                    Log.d("ApiClient", "URL для устройства: ${AppConfig.LOCAL_SERVER_URL_DEVICE}")
                    val customIp = appContext?.let { AppConfig.getCustomServerIp(it) }
                    if (!customIp.isNullOrBlank()) {
                        Log.d("ApiClient", "Используется кастомный IP: $customIp")
                    } else {
                        Log.w("ApiClient", "⚠️ ВАЖНО: Убедитесь, что LOCAL_SERVER_URL_DEVICE содержит правильный IP вашего компьютера!")
                        Log.w("ApiClient", "Текущий IP в конфиге: ${AppConfig.LOCAL_SERVER_URL_DEVICE}")
                        Log.w("ApiClient", "Как узнать IP: Windows - ipconfig, Mac/Linux - ifconfig")
                    }
                }
            } else {
                Log.w("ApiClient", "⚠️ Используется ПРОДАКШН сервер!")
                Log.d("ApiClient", "BASE_URL: ${AppConfig.BASE_URL}")
            }
            Log.d("ApiClient", "Финальный URL (будет использован): $finalUrl")
            Log.d("ApiClient", "═══════════════════════════════════════")
        } catch (e: Exception) {
            Log.e("ApiClient", "Ошибка в init блоке ApiClient", e)
            // Не падаем, просто логируем
        }
    }
    
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        try {
            HttpLoggingInterceptor { message ->
                Log.d("ApiClient", "HTTP: $message")
            }.apply {
                level = if (AppConfig.LOG_API_REQUESTS) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "Ошибка создания logging interceptor", e)
            // В случае ошибки создаем базовый интерцептор
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request()
                    Log.d("ApiClient", "→ Запрос: ${request.method} ${request.url}")
                    try {
                        val response = chain.proceed(request)
                        Log.d("ApiClient", "← Ответ: ${response.code} ${response.message} (${request.url})")
                        response
                    } catch (e: Exception) {
                        Log.e("ApiClient", "✗ Ошибка запроса к ${request.url}", e)
                        throw e
                    }
                }
                .connectTimeout(5, TimeUnit.SECONDS)  // Быстрая проверка подключения
                .readTimeout(10, TimeUnit.SECONDS)    // Не слишком долго ждем ответ
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
            Log.d("ApiClient", "OkHttpClient создан успешно")
            client
        } catch (e: Exception) {
            Log.e("ApiClient", "Ошибка создания OkHttpClient", e)
            // В случае ошибки создаем базовый клиент
            OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        }
    }
    
    private val retrofit: Retrofit by lazy {
        try {
            val baseUrl = BASE_URL
            // Проверяем, что URL валиден
            if (baseUrl.isBlank()) {
                throw IllegalArgumentException("BASE_URL не может быть пустым")
            }
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        } catch (e: Exception) {
            Log.e("ApiClient", "Ошибка создания Retrofit", e)
            // В случае ошибки пытаемся создать базовый Retrofit с fallback URL
            val fallbackUrl = if (AppConfig.USE_LOCAL_SERVER) {
                // Если включен локальный сервер, используем локальный URL
                if (AppConfig.isEmulator()) {
                    AppConfig.LOCAL_SERVER_URL_EMULATOR
                } else {
                    AppConfig.LOCAL_SERVER_URL_DEVICE
                }
            } else {
                // Иначе используем продакшн URL
                "https://api.bteu-schedule.by/v1/"
            }
            try {
                Log.w("ApiClient", "Попытка создать Retrofit с fallback URL: $fallbackUrl")
                Retrofit.Builder()
                    .baseUrl(fallbackUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            } catch (e2: Exception) {
                Log.e("ApiClient", "Критическая ошибка создания Retrofit", e2)
                // В крайнем случае создаем Retrofit без клиента
                Retrofit.Builder()
                    .baseUrl(fallbackUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
    }
    
    val scheduleApi: ScheduleApiService by lazy {
        try {
            Log.d("ApiClient", "Создание ScheduleApiService...")
            val service = retrofit.create(ScheduleApiService::class.java)
            Log.d("ApiClient", "ScheduleApiService создан успешно")
            service
        } catch (e: Exception) {
            Log.e("ApiClient", "КРИТИЧЕСКАЯ ОШИБКА создания API service", e)
            // Вместо падения создаем заглушку, чтобы приложение не падало
            // Это позволит приложению запуститься, но API запросы будут падать
            // В будущем можно добавить fallback механизм
            try {
                // Пытаемся создать базовый Retrofit без интерцепторов
                val fallbackRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                fallbackRetrofit.create(ScheduleApiService::class.java)
            } catch (e2: Exception) {
                Log.e("ApiClient", "Не удалось создать fallback API service", e2)
                // Создаем заглушку через Proxy, чтобы не падало при обращении
                java.lang.reflect.Proxy.newProxyInstance(
                    ScheduleApiService::class.java.classLoader,
                    arrayOf(ScheduleApiService::class.java)
                ) { _, _, _ ->
                    throw IllegalStateException("API service не инициализирован", e)
                } as ScheduleApiService
            }
        }
    }
}

