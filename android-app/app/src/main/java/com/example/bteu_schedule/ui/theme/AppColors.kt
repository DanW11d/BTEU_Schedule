package com.example.bteu_schedule.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Цветовая палитра приложения
 * 
 * Все цвета вынесены в отдельный объект для централизованного управления
 * и предотвращения магических значений в коде.
 * 
 * Обновлено на новую синюю цветовую систему
 */
object AppColors {
    
    // ==================== Основные градиенты ====================
    
    /**
     * Градиент главного экрана (обновлен на синий)
     */
    object HomeGradient {
        val Start = Color(0xFF3A4DFF)      // Primary Light 1
        val Middle = Color(0xFF4C6CFF)     // Primary Light 2
        val End = Color(0xFFF5F7FA)        // Плавный переход к светлому фону
    }
    
    /**
     * Градиент расписания (обновлен на синий)
     */
    object ScheduleGradient {
        val Start = Color(0xFF3A4DFF)      // Classic Blue Start
        val End = Color(0xFF000064)        // Primary Base
    }
    
    /**
     * Градиент экзаменов (обновлен на синий)
     */
    object ExamsGradient {
        val Start = Color(0xFF4C6CFF)      // Bright Blue Start
        val End = Color(0xFF000064)        // Primary Base
    }
    
    /**
     * Градиент зачетов (обновлен на синий)
     */
    object TestsGradient {
        val Start = Color(0xFF7A8BFF)      // Soft Blue Start
        val End = Color(0xFF000064)        // Primary Base
    }
    
    /**
     * Градиент звонков (обновлен на синий)
     */
    object BellScheduleGradient {
        val Start = Color(0xFF3A4DFF)      // Classic Blue Start
        val End = Color(0xFF000064)        // Primary Base
    }
    
    /**
     * Градиент кафедр (обновлен на синий)
     */
    object DepartmentsGradient {
        val Start = Color(0xFF4C6CFF)      // Bright Blue Start
        val End = Color(0xFF000064)        // Primary Base
    }
    
    // ==================== Дополнительные цвета ====================
    
    /**
     * Цвет уведомлений
     */
    object Notification {
        val Badge = Color(0xFFFF3B30)
        val BadgeAlpha = Color(0xFFFF3B30).copy(alpha = 0.5f)
    }
    
    /**
     * Цвета заголовка (обновлены на синие)
     */
    object Header {
        val Start = Color(0xFF3A4DFF).copy(alpha = 0.95f)  // Primary Light 1
        val End = Color(0xFF4C6CFF).copy(alpha = 0.9f)     // Primary Light 2
    }
    
    // ==================== Альфа-каналы ====================
    
    /**
     * Стандартные значения прозрачности
     */
    object Alpha {
        const val Opaque = 1.0f
        const val High = 0.95f
        const val Medium = 0.9f
        const val Low = 0.5f
        const val VeryLow = 0.3f
    }
    
    // ==================== Новые фирменные цвета ====================
    
    /**
     * Белый цвет с прозрачностью (для UI элементов)
     */
    object White {
        val Full = Color(0xFFFFFFFF)
        val Alpha80 = Color(0xFFFFFFFF).copy(alpha = 0.8f)
        val Alpha20 = Color(0xFFFFFFFF).copy(alpha = 0.2f)
    }
}
