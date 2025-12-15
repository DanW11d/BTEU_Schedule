
package com.example.bteu_schedule.di

import android.content.Context
import androidx.room.Room
import com.example.bteu_schedule.data.local.database.ScheduleDatabase
import com.example.bteu_schedule.data.local.database.dao.ExamDao
import com.example.bteu_schedule.data.local.database.migration.ALL_MIGRATIONS
import com.example.bteu_schedule.data.local.database.dao.FacultyDao
import com.example.bteu_schedule.data.local.database.dao.GroupDao
import com.example.bteu_schedule.data.local.database.dao.LessonDao
import com.example.bteu_schedule.data.config.AppConfig
import com.example.bteu_schedule.data.remote.OpenAiService
import com.example.bteu_schedule.data.remote.api.OpenAiApiService
import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.data.repository.ScheduleRepository
import com.example.bteu_schedule.data.website.WebsiteScheduleRepository
import com.example.bteu_schedule.domain.service.ScheduleService
import com.example.bteu_schedule.ui.theme.ThemeManager
import com.example.bteu_schedule.utils.NetworkRetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Theme Module ---
    @Provides
    @Singleton
    fun provideThemeManager(@ApplicationContext context: Context): ThemeManager {
        return ThemeManager(context)
    }

    // --- OpenAI Module ---
    @Provides
    @Singleton
    fun provideOpenAiApiService(): OpenAiApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Логируем только если явно включено в AppConfig
            level = if (AppConfig.LOG_API_REQUESTS) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        // Retry интерцептор для автоматических повторных попыток при сетевых ошибках
        val retryInterceptor = NetworkRetryInterceptor(
            maxRetries = 2, // Меньше попыток для OpenAI (платный API)
            initialDelayMs = 1000L,
            maxDelayMs = 5000L
        )
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(retryInterceptor) // Добавляем retry перед logging
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(OpenAiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenAiService(
        @ApplicationContext context: Context,
        openAiApiService: OpenAiApiService
    ): OpenAiService {
        return OpenAiService(context, openAiApiService)
    }

    // --- ApiModule --- 

    /**
     * Получает базовый URL API из конфигурации
     * Использует AppConfig для определения правильного URL (production/local)
     */
    private fun getBaseUrl(context: Context?): String {
        return if (AppConfig.USE_LOCAL_SERVER) {
            // Используем локальный сервер для разработки
            AppConfig.getLocalServerUrl(context)
        } else {
            // Используем продакшн сервер
            AppConfig.BASE_URL
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Логируем только если явно включено в AppConfig
            level = if (AppConfig.LOG_API_REQUESTS) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        // Retry интерцептор для автоматических повторных попыток при сетевых ошибках
        val retryInterceptor = NetworkRetryInterceptor(
            maxRetries = 3,
            initialDelayMs = 1000L,
            maxDelayMs = 10000L
        )
        
        return OkHttpClient.Builder()
            .addInterceptor(retryInterceptor) // Добавляем retry перед logging
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): Retrofit {
        val baseUrl = getBaseUrl(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideScheduleApiService(retrofit: Retrofit): ScheduleApiService {
        return retrofit.create(ScheduleApiService::class.java)
    }

    // --- DatabaseModule --- 

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): ScheduleDatabase {
        return try {
            Room.databaseBuilder(
                context,
                ScheduleDatabase::class.java,
                "schedule_database"
            )
                .addMigrations(*ALL_MIGRATIONS)
                // Временно добавлен fallback для диагностики крашей
                // TODO: Убрать после исправления проблемы с миграциями
                .fallbackToDestructiveMigration()
                .build()
        } catch (e: Exception) {
            // Логируем ошибку и пробуем создать БД без миграций
            com.example.bteu_schedule.utils.AppLogger.e("AppModule", "Ошибка создания базы данных, используем fallback", e)
            Room.databaseBuilder(
            context,
            ScheduleDatabase::class.java,
            "schedule_database"
        )
            .fallbackToDestructiveMigration()
            .build()
        }
    }

    @Provides
    fun provideFacultyDao(appDatabase: ScheduleDatabase): FacultyDao {
        return appDatabase.facultyDao()
    }

    @Provides
    fun provideGroupDao(appDatabase: ScheduleDatabase): GroupDao {
        return appDatabase.groupDao()
    }

    @Provides
    fun provideLessonDao(appDatabase: ScheduleDatabase): LessonDao {
        return appDatabase.lessonDao()
    }

    @Provides
    fun provideExamDao(appDatabase: ScheduleDatabase): ExamDao {
        return appDatabase.examDao()
    }

    // --- Repository Module --- 

    @Provides
    @Singleton
    fun provideWebsiteScheduleRepository(): WebsiteScheduleRepository {
        return WebsiteScheduleRepository()
    }

    @Provides
    @Singleton
    fun provideCachedScheduleRepository(
        facultyDao: FacultyDao,
        groupDao: GroupDao,
        lessonDao: LessonDao,
        examDao: ExamDao,
        apiService: ScheduleApiService,
        websiteRepository: WebsiteScheduleRepository
    ): CachedScheduleRepository {
        return CachedScheduleRepository(facultyDao, groupDao, lessonDao, examDao, apiService, websiteRepository)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        apiService: ScheduleApiService,
        websiteRepository: WebsiteScheduleRepository
    ): ScheduleRepository {
        return ScheduleRepository(apiService, websiteRepository)
    }

    // --- Schedule Service Module ---
    @Provides
    @Singleton
    fun provideScheduleService(): ScheduleService {
        return ScheduleService()
    }
}
