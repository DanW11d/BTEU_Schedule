package com.example.bteu_schedule.utils

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Утилита для расчета номера недели от начала учебного года
 * Учебный год начинается 1 сентября
 */
object WeekCalculator {
    
    /**
     * Дата начала учебного года
     */
    private const val ACADEMIC_YEAR_START_MONTH = Calendar.SEPTEMBER  // Сентябрь (8 в Calendar, т.к. месяцы с 0)
    private const val ACADEMIC_YEAR_START_DAY = 1
    
    /**
     * Получить номер текущей недели от начала учебного года
     * Неделя начинается с понедельника
     * 
     * @return Номер недели (начинается с 1)
     */
    fun getCurrentWeekNumber(): Int {
        val today = Calendar.getInstance()
        return getWeekNumberForDate(today)
    }
    
    /**
     * Получить номер недели для указанной даты
     * 
     * @param date Calendar с датой для расчета
     * @return Номер недели (начинается с 1)
     */
    fun getWeekNumberForDate(date: Calendar): Int {
        return try {
            // Определяем начало учебного года
            val currentYear = try {
                date.get(Calendar.YEAR)
            } catch (e: Exception) {
                android.util.Log.e("WeekCalculator", "Ошибка получения года", e)
                Calendar.getInstance().get(Calendar.YEAR)
            }
            
            val academicYearStart = try {
                GregorianCalendar(currentYear, ACADEMIC_YEAR_START_MONTH, ACADEMIC_YEAR_START_DAY)
            } catch (e: Exception) {
                android.util.Log.e("WeekCalculator", "Ошибка создания календаря", e)
                return 1 // Fallback
            }
        
        // Если текущая дата раньше 1 сентября, берем начало предыдущего учебного года
        val startDate = if (date.before(academicYearStart)) {
            GregorianCalendar(currentYear - 1, ACADEMIC_YEAR_START_MONTH, ACADEMIC_YEAR_START_DAY)
        } else {
            academicYearStart
        }
        
        // Находим понедельник недели, в которой находится дата начала учебного года
        val startMonday = getMondayOfWeek(startDate)
        
        // Находим понедельник недели, в которой находится текущая дата
        val currentMonday = getMondayOfWeek(date)
        
            // Вычисляем количество недель между понедельниками
            val daysBetween = try {
                ((currentMonday.timeInMillis - startMonday.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            } catch (e: Exception) {
                android.util.Log.e("WeekCalculator", "Ошибка вычисления дней", e)
                return 1 // Fallback
            }
            
            val weeksBetween = daysBetween / 7
            
            // Номер недели (начинается с 1)
            (weeksBetween + 1).coerceAtLeast(1)
        } catch (e: Exception) {
            android.util.Log.e("WeekCalculator", "Критическая ошибка в getWeekNumberForDate", e)
            1 // Fallback на первую неделю
        }
    }
    
    /**
     * Получить понедельник недели для указанной даты
     */
    private fun getMondayOfWeek(date: Calendar): Calendar {
        val monday = try {
            date.clone() as Calendar
        } catch (e: Exception) {
            android.util.Log.e("WeekCalculator", "Ошибка клонирования календаря", e)
            return Calendar.getInstance() // Fallback
        }
        
        val dayOfWeek = try {
            monday.get(Calendar.DAY_OF_WEEK)
        } catch (e: Exception) {
            android.util.Log.e("WeekCalculator", "Ошибка получения дня недели", e)
            Calendar.MONDAY // Fallback на понедельник
        }
        // Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ..., Calendar.SATURDAY = 7
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) {
            6  // Воскресенье - 6 дней назад до понедельника
        } else {
            dayOfWeek - Calendar.MONDAY  // Понедельник = 0, Вторник = 1, и т.д.
        }
        monday.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        monday.set(Calendar.HOUR_OF_DAY, 0)
        monday.set(Calendar.MINUTE, 0)
        monday.set(Calendar.SECOND, 0)
        monday.set(Calendar.MILLISECOND, 0)
        return monday
    }
    
    /**
     * Определить, является ли неделя нечетной
     * Нечетная неделя = нечетный номер недели
     * 
     * @param weekNumber Номер недели
     * @return true если нечетная, false если четная
     */
    fun isOddWeek(weekNumber: Int): Boolean {
        return weekNumber % 2 == 1
    }
    
    /**
     * Получить четность текущей недели
     * 
     * @return true если нечетная, false если четная
     */
    fun isCurrentWeekOdd(): Boolean {
        return isOddWeek(getCurrentWeekNumber())
    }
    
    /**
     * Определить четность недели для LocalDate
     * @param date Дата для проверки
     * @return true если нечетная, false если четная
     */
    fun isWeekOdd(date: java.time.LocalDate): Boolean {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
        }
        return isOddWeek(getWeekNumberForDate(calendar))
    }
}

