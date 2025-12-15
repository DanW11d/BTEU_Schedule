package com.example.bteu_schedule.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.OnboardingStep
import com.example.bteu_schedule.ui.navigation.AppDestinations
import com.google.gson.Gson

/**
 * Менеджер для сохранения и восстановления состояния приложения
 */
class AppStateManager(private val context: Context) {
    
    private val prefs: SharedPreferences by lazy {
        try {
            context.getSharedPreferences(
                "app_state_prefs",
                Context.MODE_PRIVATE
            )
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка создания SharedPreferences", e)
            // Пытаемся создать с контекстом приложения
            context.applicationContext.getSharedPreferences(
                "app_state_prefs",
                Context.MODE_PRIVATE
            )
        }
    }
    
    private val gson = Gson()
    
    // Ключи для SharedPreferences
    private companion object {
        const val KEY_ONBOARDING_STEP = "onboarding_step"
        const val KEY_FACULTY_ID = "faculty_id"
        const val KEY_FACULTY_CODE = "faculty_code"
        const val KEY_EDUCATION_FORM = "education_form"
        const val KEY_DEPARTMENT_ID = "department_id"
        const val KEY_COURSE = "course"
        const val KEY_CURRENT_GROUP = "current_group"
        const val KEY_CURRENT_DESTINATION = "current_destination"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_SCHEDULE_UPDATES_ENABLED = "schedule_updates_enabled"
    }
    
    /**
     * Сохранить шаг онбординга
     */
    fun saveOnboardingStep(step: OnboardingStep) {
        try {
            prefs.edit().putString(KEY_ONBOARDING_STEP, step.name).apply()
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения onboardingStep", e)
        }
    }
    
    /**
     * Загрузить шаг онбординга
     */
    fun loadOnboardingStep(): OnboardingStep {
        return try {
            val stepName = prefs.getString(KEY_ONBOARDING_STEP, OnboardingStep.MAIN.name)
            OnboardingStep.valueOf(stepName ?: OnboardingStep.MAIN.name)
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки onboardingStep", e)
            OnboardingStep.MAIN
        }
    }
    
    /**
     * Сохранить выбранный факультет
     */
    fun saveFaculty(id: Int?, code: String?) {
        try {
            prefs.edit().apply {
                if (id != null) putInt(KEY_FACULTY_ID, id) else remove(KEY_FACULTY_ID)
                if (code != null) putString(KEY_FACULTY_CODE, code) else remove(KEY_FACULTY_CODE)
                apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения faculty", e)
        }
    }
    
    /**
     * Загрузить выбранный факультет
     */
    fun loadFaculty(): Pair<Int?, String?> {
        return try {
            val id = if (prefs.contains(KEY_FACULTY_ID)) prefs.getInt(KEY_FACULTY_ID, -1) else null
            val code = prefs.getString(KEY_FACULTY_CODE, null)
            Pair(if (id != null && id >= 0) id else null, code)
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки faculty", e)
            Pair(null, null)
        }
    }
    
    /**
     * Сохранить форму обучения
     */
    fun saveEducationForm(form: String?) {
        try {
            prefs.edit().apply {
                if (form != null) putString(KEY_EDUCATION_FORM, form) else remove(KEY_EDUCATION_FORM)
                apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения educationForm", e)
        }
    }
    
    /**
     * Загрузить форму обучения
     */
    fun loadEducationForm(): String? {
        return try {
            prefs.getString(KEY_EDUCATION_FORM, null)
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки educationForm", e)
            null
        }
    }
    
    /**
     * Сохранить кафедру
     */
    fun saveDepartmentId(id: Int?) {
        try {
            prefs.edit().apply {
                if (id != null) putInt(KEY_DEPARTMENT_ID, id) else remove(KEY_DEPARTMENT_ID)
                apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения departmentId", e)
        }
    }
    
    /**
     * Загрузить кафедру
     */
    fun loadDepartmentId(): Int? {
        return try {
            if (prefs.contains(KEY_DEPARTMENT_ID)) prefs.getInt(KEY_DEPARTMENT_ID, -1) else null
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки departmentId", e)
            null
        }
    }
    
    /**
     * Сохранить курс
     */
    fun saveCourse(course: Int?) {
        try {
            prefs.edit().apply {
                if (course != null) putInt(KEY_COURSE, course) else remove(KEY_COURSE)
                apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения course", e)
        }
    }
    
    /**
     * Загрузить курс
     */
    fun loadCourse(): Int? {
        return try {
            if (prefs.contains(KEY_COURSE)) prefs.getInt(KEY_COURSE, -1) else null
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки course", e)
            null
        }
    }
    
    /**
     * Сохранить текущую группу
     */
    fun saveCurrentGroup(group: GroupUi?) {
        try {
            prefs.edit().apply {
                if (group != null) {
                    val json = gson.toJson(group)
                    putString(KEY_CURRENT_GROUP, json)
                } else {
                    remove(KEY_CURRENT_GROUP)
                }
                apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения currentGroup", e)
        }
    }
    
    /**
     * Загрузить текущую группу
     */
    fun loadCurrentGroup(): GroupUi? {
        val json = try {
            prefs.getString(KEY_CURRENT_GROUP, null)
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка чтения JSON группы", e)
            return null
        } ?: return null
        
        if (json.isBlank()) return null
        
        return try {
            val group = gson.fromJson(json, GroupUi::class.java)
            // Проверяем, что группа валидна (имеет все необходимые поля)
            if (group != null && group.code.isNotBlank() && group.course > 0) {
                group
            } else {
                android.util.Log.w("AppStateManager", "Группа невалидна: code=${group?.code}, course=${group?.course}")
                // Очищаем невалидные данные
                try {
                    prefs.edit().remove(KEY_CURRENT_GROUP).apply()
                } catch (e: Exception) {
                    android.util.Log.e("AppStateManager", "Ошибка очистки невалидной группы", e)
                }
                null
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            android.util.Log.e("AppStateManager", "Ошибка синтаксиса JSON при десериализации группы", e)
            // Очищаем поврежденные данные
            try {
                prefs.edit().remove(KEY_CURRENT_GROUP).apply()
            } catch (e2: Exception) {
                android.util.Log.e("AppStateManager", "Ошибка очистки поврежденных данных", e2)
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка десериализации группы", e)
            // Очищаем поврежденные данные
            try {
                prefs.edit().remove(KEY_CURRENT_GROUP).apply()
            } catch (e2: Exception) {
                android.util.Log.e("AppStateManager", "Ошибка очистки поврежденных данных", e2)
            }
            null
        }
    }
    
    /**
     * Сохранить текущий экран навигации
     */
    fun saveCurrentDestination(destination: AppDestinations) {
        try {
            prefs.edit().putString(KEY_CURRENT_DESTINATION, destination.name).apply()
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения currentDestination", e)
        }
    }
    
    /**
     * Загрузить текущий экран навигации
     */
    fun loadCurrentDestination(): AppDestinations {
        return try {
            val destName = prefs.getString(KEY_CURRENT_DESTINATION, AppDestinations.HOME.name)
            // Миграция: если сохранено старое значение "SCHEDULE", возвращаем HOME
            if (destName == "SCHEDULE") {
                AppDestinations.HOME
            } else {
                AppDestinations.valueOf(destName ?: AppDestinations.HOME.name)
            }
        } catch (e: IllegalArgumentException) {
            // Если значение не найдено в enum (например, старое "SCHEDULE"), возвращаем HOME
            android.util.Log.w("AppStateManager", "Неизвестное значение destination, используем HOME", e)
            AppDestinations.HOME
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки currentDestination", e)
            AppDestinations.HOME
        }
    }
    
    /**
     * Очистить все сохраненные данные
     */
    fun clearAll() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка очистки данных", e)
        }
    }
    
    /**
     * Проверить, есть ли сохраненное состояние
     */
    fun hasSavedState(): Boolean {
        return prefs.contains(KEY_CURRENT_GROUP) && 
               prefs.getString(KEY_CURRENT_GROUP, null) != null
    }
    
    /**
     * Сохранить состояние уведомлений о парах
     */
    fun saveNotificationsEnabled(enabled: Boolean) {
        try {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения notificationsEnabled", e)
        }
    }
    
    /**
     * Загрузить состояние уведомлений о парах
     */
    fun loadNotificationsEnabled(): Boolean {
        return try {
            prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки notificationsEnabled", e)
            true
        }
    }
    
    /**
     * Сохранить состояние уведомлений об изменениях расписания
     */
    fun saveScheduleUpdatesEnabled(enabled: Boolean) {
        try {
            prefs.edit().putBoolean(KEY_SCHEDULE_UPDATES_ENABLED, enabled).apply()
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка сохранения scheduleUpdatesEnabled", e)
        }
    }
    
    /**
     * Загрузить состояние уведомлений об изменениях расписания
     */
    fun loadScheduleUpdatesEnabled(): Boolean {
        return try {
            prefs.getBoolean(KEY_SCHEDULE_UPDATES_ENABLED, true)
        } catch (e: Exception) {
            android.util.Log.e("AppStateManager", "Ошибка загрузки scheduleUpdatesEnabled", e)
            true
        }
    }
}

