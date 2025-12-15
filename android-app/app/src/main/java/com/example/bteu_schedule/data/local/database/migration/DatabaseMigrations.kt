package com.example.bteu_schedule.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Миграции базы данных для Room
 * 
 * ВАЖНО: При добавлении новой миграции:
 * 1. Увеличьте версию БД в @Database annotation
 * 2. Добавьте новую миграцию с правильными версиями (from, to)
 * 3. Зарегистрируйте миграцию в ScheduleDatabase
 * 4. Протестируйте миграцию на реальных данных
 */

/**
 * Миграция с версии 1 на версию 2
 * 
 * История изменений:
 * - Добавлена поддержка составного первичного ключа для групп (code + facultyCode)
 * - Добавлены поля для кафедр (departmentId, departmentName)
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Миграция с версии 1 на версию 2:
        // - Изменение первичного ключа cached_groups на составной (code, facultyCode)
        // - Добавление полей departmentId и departmentName (если их еще нет)
        
        // Проверяем текущую структуру таблицы cached_groups
        val cursor = database.query("PRAGMA table_info(cached_groups)")
        val columns = mutableSetOf<String>()
        cursor.use {
            val nameIndex = it.getColumnIndexOrThrow("name")
            while (it.moveToNext()) {
                columns.add(it.getString(nameIndex))
            }
        }

        // Создаем новую таблицу с правильной структурой
        database.execSQL("""
            CREATE TABLE cached_groups_new (
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                course INTEGER NOT NULL,
                specialization TEXT NOT NULL,
                facultyCode TEXT NOT NULL,
                facultyName TEXT,
                educationForm TEXT,
                departmentId INTEGER,
                departmentName TEXT,
                cachedAt INTEGER NOT NULL,
                PRIMARY KEY(code, facultyCode)
            )
        """.trimIndent())

        // Определяем, какие столбцы нужно копировать
        val hasFacultyCode = columns.contains("facultyCode")
        val hasDepartmentId = columns.contains("departmentId")
        val hasDepartmentName = columns.contains("departmentName")
        
        // Строим SELECT запрос в зависимости от наличия столбцов
        val selectColumns = mutableListOf<String>()
        selectColumns.add("code")
        selectColumns.add("name")
        selectColumns.add("course")
        selectColumns.add("specialization")
        
        // facultyCode обязателен, используем COALESCE для безопасности
        if (hasFacultyCode) {
            selectColumns.add("COALESCE(facultyCode, '') AS facultyCode")
        } else {
            selectColumns.add("'' AS facultyCode")
        }
        
        selectColumns.add("facultyName")
        selectColumns.add("educationForm")
        
        if (hasDepartmentId) {
            selectColumns.add("departmentId")
        } else {
            selectColumns.add("NULL AS departmentId")
        }
        
        if (hasDepartmentName) {
            selectColumns.add("departmentName")
        } else {
            selectColumns.add("NULL AS departmentName")
        }
        
        selectColumns.add("cachedAt")

        // Копируем данные из старой таблицы
        val selectQuery = """
            INSERT INTO cached_groups_new (
                code, name, course, specialization, 
                facultyCode, facultyName, educationForm,
                departmentId, departmentName, cachedAt
            )
            SELECT ${selectColumns.joinToString(", ")}
            FROM cached_groups
        """.trimIndent()
        
        database.execSQL(selectQuery)

        // Удаляем старую таблицу
        database.execSQL("DROP TABLE cached_groups")

        // Переименовываем новую таблицу
        database.execSQL("ALTER TABLE cached_groups_new RENAME TO cached_groups")

        // Создаем индексы (если их еще нет)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_cached_groups_code 
            ON cached_groups(code)
        """.trimIndent())
        
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_cached_groups_facultyCode 
            ON cached_groups(facultyCode)
        """.trimIndent())
    }
}

/**
 * Миграция с версии 2 на версию 3
 * 
 * История изменений:
 * - Добавлены составные индексы для оптимизации запросов к таблице cached_groups
 * - Добавлен индекс на поле course для фильтрации по курсу
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем составные индексы для оптимизации запросов
        
        // Индекс для запроса по facultyCode и educationForm
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_cached_groups_facultyCode_educationForm 
            ON cached_groups(facultyCode, educationForm)
        """.trimIndent())
        
        // Индекс для запроса по facultyCode, educationForm и course
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_cached_groups_facultyCode_educationForm_course 
            ON cached_groups(facultyCode, educationForm, course)
        """.trimIndent())
        
        // Индекс для фильтрации по курсу
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_cached_groups_course 
            ON cached_groups(course)
        """.trimIndent())
    }
}

/**
 * Список всех миграций базы данных
 * Используется для регистрации в Room Database Builder
 */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3
    // Добавляйте новые миграции здесь при увеличении версии БД
    // Например: MIGRATION_3_4, MIGRATION_4_5, и т.д.
)

