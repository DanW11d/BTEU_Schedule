
package com.example.bteu_schedule.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.bteu_schedule.data.local.database.ScheduleDatabase
import com.example.bteu_schedule.data.local.database.dao.*
import com.example.bteu_schedule.data.local.database.mapper.*
import com.example.bteu_schedule.data.mock.*
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InitialDataLoader(
    private val context: Context,
    private val repository: CachedScheduleRepository? = null
) {

    companion object {
        private const val TAG = "InitialDataLoader"
        private const val PREF_NAME = "app_prefs"
        private const val KEY_INITIAL_DATA_LOADED = "initial_data_loaded"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val database: ScheduleDatabase = ScheduleDatabase.getDatabase(context)
    private val facultyDao: FacultyDao = database.facultyDao()
    private val groupDao: GroupDao = database.groupDao()
    private val lessonDao: LessonDao = database.lessonDao()

    suspend fun isInitialDataLoaded(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY_INITIAL_DATA_LOADED, false) && facultyDao.getFaculties().isNotEmpty()
    }

    suspend fun loadInitialData(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialDataLoaded()) {
            Log.d(TAG, "Начальные данные уже загружены, пропускаем")
            return@withContext true
        }

        Log.d(TAG, "НАЧАЛО ЗАГРУЗКИ НАЧАЛЬНЫХ ДАННЫХ")
        NetworkUtils.logConnectionInfo(context)

        val serverSyncSuccess = repository?.let { repo ->
            if (NetworkUtils.isConnected(context)) {
                try {
                    Log.d(TAG, "Попытка синхронизации с сервером (FTP/API)...")
                    val result = repo.syncAllDataFromServer()
                    val success = result is ApiResponse.Success<*>
                    if (success) {
                        Log.d(TAG, "Синхронизация с сервером успешна")
                    } else {
                        Log.w(TAG, "Синхронизация с сервером не удалась: ${(result as? ApiResponse.Error)?.message}")
                    }
                    success
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при синхронизации: ${e.message}", e)
                    false
                }
            } else {
                Log.d(TAG, "Нет подключения к интернету для синхронизации")
                false
            }
        } ?: false

        if (!serverSyncSuccess) {
            Log.d(TAG, "Загружаем мок-данные...")
            loadMockData()
        }

        val dataLoaded = facultyDao.getFaculties().isNotEmpty() && groupDao.getAllGroupsList().isNotEmpty()
        if (dataLoaded) {
            prefs.edit().putBoolean(KEY_INITIAL_DATA_LOADED, true).apply()
            Log.d(TAG, "ЗАГРУЗКА НАЧАЛЬНЫХ ДАННЫХ ЗАВЕРШЕНА")
        }
        dataLoaded
    }

    private suspend fun loadMockData() = withContext(Dispatchers.IO) {
        try {
            val faculties = mockFaculties()
            facultyDao.insertFaculties(faculties.map { it.toEntity() })

            val allGroups = mutableListOf<com.example.bteu_schedule.domain.models.GroupUi>()
            allGroups.addAll(mockGroupsFor("FEU", "full_time"))
            allGroups.addAll(mockGroupsFor("FEU", "part_time"))
            allGroups.addAll(mockGroupsFor("FKIF", "full_time"))
            allGroups.addAll(mockGroupsFor("FKIF", "part_time"))
            groupDao.insertGroups(allGroups.map { it.toEntity() })

            val allLessons = allGroups.flatMap { group ->
                (0..5).flatMap { dayIndex ->
                    val oddLessons = mockLessonsForDay(group.code, dayIndex, isOddWeek = true)
                    val evenLessons = mockLessonsForDay(group.code, dayIndex, isOddWeek = false)
                    (oddLessons + evenLessons).map { it.toEntity(group.code) }
                }
            }.distinct()
            lessonDao.insertLessons(allLessons)
            Log.d(TAG, "Мок-данные загружены")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки мок-данных", e)
        }
    }
}
