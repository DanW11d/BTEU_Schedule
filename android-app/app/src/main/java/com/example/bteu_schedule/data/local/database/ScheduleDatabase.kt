package com.example.bteu_schedule.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bteu_schedule.data.local.database.dao.*
import com.example.bteu_schedule.data.local.database.entity.*
import com.example.bteu_schedule.data.local.database.migration.ALL_MIGRATIONS

/**
 * Room Database для кеширования данных расписания
 */
@Database(
    entities = [
        CachedFaculty::class,
        CachedGroup::class,
        CachedLesson::class,
        CachedExam::class
    ],
    version = 3,
    exportSchema = true
)
abstract class ScheduleDatabase : RoomDatabase() {
    
    abstract fun facultyDao(): FacultyDao
    abstract fun groupDao(): GroupDao
    abstract fun lessonDao(): LessonDao
    abstract fun examDao(): ExamDao
    
    companion object {
        @Volatile
        private var INSTANCE: ScheduleDatabase? = null
        private const val DATABASE_NAME = "schedule_database"
        
        fun getDatabase(context: Context): ScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    // НЕ используем fallbackToDestructiveMigration для production
                    // Это приведет к потере данных пользователей при обновлениях
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun clearDatabase(context: Context) {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

