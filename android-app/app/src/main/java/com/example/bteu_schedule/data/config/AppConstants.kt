package com.example.bteu_schedule.data.config

/**
 * Константы приложения
 * 
 * Содержит все магические числа и строки, используемые в приложении.
 * Улучшает читаемость кода и упрощает поддержку.
 */

object AppConstants {
    
    // ==================== Сетевые константы ====================
    
    /**
     * Максимальное количество попыток повтора сетевого запроса
     */
    const val NETWORK_MAX_RETRIES = 3
    
    /**
     * Начальная задержка перед повторной попыткой (в миллисекундах)
     */
    const val NETWORK_INITIAL_RETRY_DELAY_MS = 1000L
    
    /**
     * Максимальная задержка перед повторной попыткой (в миллисекундах)
     */
    const val NETWORK_MAX_RETRY_DELAY_MS = 10000L
    
    /**
     * Таймаут подключения (в секундах)
     */
    const val NETWORK_CONNECT_TIMEOUT_SECONDS = 30L
    
    /**
     * Таймаут чтения (в секундах)
     */
    const val NETWORK_READ_TIMEOUT_SECONDS = 30L
    
    /**
     * Таймаут записи (в секундах)
     */
    const val NETWORK_WRITE_TIMEOUT_SECONDS = 30L
    
    // ==================== HTTP коды ошибок для повтора ====================
    
    /**
     * HTTP коды, при которых запрос будет повторен
     */
    val RETRYABLE_HTTP_CODES = listOf(
        408, // Request Timeout
        429, // Too Many Requests
        500, // Internal Server Error
        502, // Bad Gateway
        503, // Service Unavailable
        504  // Gateway Timeout
    )
    
    // ==================== Константы изображений ====================
    
    /**
     * Максимальная ширина изображения для оптимизации (в пикселях)
     */
    const val IMAGE_MAX_WIDTH = 1920
    
    /**
     * Максимальная высота изображения для оптимизации (в пикселях)
     */
    const val IMAGE_MAX_HEIGHT = 1920
    
    /**
     * Качество сжатия WebP (0-100, где 100 - лучшее качество)
     */
    const val IMAGE_WEBP_QUALITY = 85
    
    /**
     * Максимальный размер изображения в KB
     */
    const val IMAGE_MAX_SIZE_KB = 500
    
    /**
     * Процент доступной памяти для кэша изображений в памяти
     */
    const val IMAGE_MEMORY_CACHE_SIZE_PERCENT = 0.25f
    
    /**
     * Максимальный размер дискового кэша изображений (в байтах)
     */
    const val IMAGE_DISK_CACHE_SIZE_BYTES = 250L * 1024 * 1024 // 250 MB
    
    // ==================== Константы кэша БД ====================
    
    /**
     * Время жизни кэша групп в миллисекундах (5 минут)
     */
    const val CACHE_GROUPS_TTL_MS = 5L * 60 * 1000
    
    /**
     * Время жизни кэша факультетов в миллисекундах (10 минут)
     */
    const val CACHE_FACULTIES_TTL_MS = 10L * 60 * 1000
    
    /**
     * Время жизни кэша расписания в миллисекундах (1 час)
     */
    const val CACHE_SCHEDULE_TTL_MS = 60L * 60 * 1000
    
    /**
     * Время жизни кэша экзаменов в миллисекундах (30 минут)
     */
    const val CACHE_EXAMS_TTL_MS = 30L * 60 * 1000
    
    // ==================== Константы БД ====================
    
    /**
     * Текущая версия базы данных
     */
    const val DATABASE_VERSION = 3
    
    /**
     * Имя базы данных
     */
    const val DATABASE_NAME = "schedule_database"
    
    // ==================== Константы анимаций ====================
    
    /**
     * Длительность стандартной анимации (в миллисекундах)
     */
    const val ANIMATION_DURATION_DEFAULT_MS = 300
    
    /**
     * Задержка между анимациями элементов списка (в миллисекундах)
     */
    const val ANIMATION_STAGGER_DELAY_MS = 50
    
    /**
     * Длительность анимации появления экрана (в миллисекундах)
     */
    const val ANIMATION_SCREEN_FADE_IN_MS = 600
    
    /**
     * Начальная задержка анимации экрана (в миллисекундах)
     */
    const val ANIMATION_SCREEN_START_DELAY_MS = 100
    
    // ==================== Константы пагинации ====================
    
    /**
     * Размер страницы по умолчанию для пагинации
     */
    const val PAGINATION_DEFAULT_PAGE_SIZE = 20
    
    /**
     * Максимальный размер страницы для пагинации
     */
    const val PAGINATION_MAX_PAGE_SIZE = 100
    
    // ==================== Константы форматов данных ====================
    
    /**
     * Размер килобайта в байтах
     */
    const val BYTES_PER_KB = 1024
    
    /**
     * Размер мегабайта в байтах
     */
    const val BYTES_PER_MB = 1024 * 1024
    
    /**
     * Размер гигабайта в байтах
     */
    const val BYTES_PER_GB = 1024 * 1024 * 1024
    
    // ==================== Константы файлов ====================
    
    /**
     * Имя файла настроек SharedPreferences
     */
    const val PREF_FILE_NAME = "app_config"
    
    /**
     * Ключ для кастомного IP сервера в SharedPreferences
     */
    const val PREF_KEY_CUSTOM_SERVER_IP = "custom_server_ip"
    
    /**
     * Имя директории для кэша изображений
     */
    const val IMAGE_CACHE_DIR_NAME = "image_cache"
    
    // ==================== Константы валидации ====================
    
    /**
     * Минимальный курс
     */
    const val MIN_COURSE = 1
    
    /**
     * Максимальный курс
     */
    const val MAX_COURSE = 6
    
    /**
     * Минимальная длина названия группы
     */
    const val MIN_GROUP_NAME_LENGTH = 2
    
    /**
     * Максимальная длина названия группы
     */
    const val MAX_GROUP_NAME_LENGTH = 50
}

