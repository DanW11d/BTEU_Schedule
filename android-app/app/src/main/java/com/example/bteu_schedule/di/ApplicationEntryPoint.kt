package com.example.bteu_schedule.di

import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint для получения зависимостей в Application классе
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApplicationEntryPoint {
    fun getCachedScheduleRepository(): CachedScheduleRepository
}

