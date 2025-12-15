package com.example.bteu_schedule.data.remote

import android.util.Log
import com.example.bteu_schedule.data.remote.api.OpenAiApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Клиент для работы с OpenAI API
 */
object OpenAiClient {
    
    private const val BASE_URL = "https://api.openai.com/v1/"
    
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            Log.d("OpenAiClient", "HTTP: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("OpenAiClient", "→ Запрос: ${request.method} ${request.url}")
                try {
                    val response = chain.proceed(request)
                    Log.d("OpenAiClient", "← Ответ: ${response.code} ${response.message}")
                    response
                } catch (e: Exception) {
                    Log.e("OpenAiClient", "✗ Ошибка запроса", e)
                    throw e
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val openAiApi: OpenAiApiService by lazy {
        retrofit.create(OpenAiApiService::class.java)
    }
}

