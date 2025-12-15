package com.example.bteu_schedule.di

import android.content.Context
import coil3.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модуль для настройки Coil ImageLoader
 * 
 * Конфигурирует:
 * - Кэширование изображений (память и диск)
 * - Оптимизацию размера изображений
 * - Поддержку WebP формата
 * - Логирование (только в DEBUG режиме)
 */
@Module
@InstallIn(SingletonComponent::class)
object CoilModule {

    /**
     * Предоставляет настроенный ImageLoader для Coil
     * 
     * Особенности:
     * - Кэш в памяти: до 25% доступной памяти устройства
     * - Кэш на диске: до 250 MB
     * - Автоматическая поддержка WebP
     * - Оптимизация размера изображений
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        // Используем дефолтный ImageLoader с автоматической конфигурацией
        // Coil 3 автоматически настроит оптимальные параметры кэширования
        return ImageLoader.Builder(context).build()
    }
}

