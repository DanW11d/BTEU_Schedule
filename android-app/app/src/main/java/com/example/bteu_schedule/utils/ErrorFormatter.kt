package com.example.bteu_schedule.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

/**
 * Утилита для форматирования ошибок сети в понятные сообщения для пользователя
 */
object ErrorFormatter {
    
    /**
     * Форматирует исключение в понятное сообщение для пользователя
     */
    fun formatError(exception: Throwable?): String {
        if (exception == null) {
            return "Неизвестная ошибка"
        }
        
        return when (exception) {
            is ConnectException -> {
                val message = exception.message ?: ""
                val baseUrl = if (com.example.bteu_schedule.data.config.AppConfig.USE_LOCAL_SERVER) {
                    if (com.example.bteu_schedule.data.config.AppConfig.isEmulator()) {
                        com.example.bteu_schedule.data.config.AppConfig.LOCAL_SERVER_URL_EMULATOR
                    } else {
                        com.example.bteu_schedule.data.config.AppConfig.LOCAL_SERVER_URL_DEVICE
                    }
                } else {
                    com.example.bteu_schedule.data.config.AppConfig.BASE_URL
                }
                when {
                    message.contains("10.0.2.2") || message.contains("localhost") || message.contains("127.0.0.1") || message.contains("192.168.") || message.contains("100.70.") -> {
                        "Не удалось подключиться к локальному серверу.\n\n" +
                        "Убедитесь, что:\n" +
                        "• Сервер запущен: python backend/server.py\n" +
                        "• Сервер доступен по адресу: $baseUrl\n" +
                        "• Для эмулятора: http://10.0.2.2:8000/v1/\n" +
                        "• Для реального устройства: http://[IP_компьютера]:8000/v1/\n" +
                        "• Устройство и компьютер в одной Wi-Fi сети"
                    }
                    message.contains("timeout") || message.contains("timed out") -> {
                        "Превышено время ожидания подключения.\n" +
                        "Проверьте подключение к интернету и доступность сервера."
                    }
                    else -> {
                        val detailedMessage = if (com.example.bteu_schedule.data.config.AppConfig.USE_LOCAL_SERVER) {
                            val isEmulator = com.example.bteu_schedule.data.config.AppConfig.isEmulator()
                            val expectedIp = if (isEmulator) "10.0.2.2" else "192.168.1.25"
                            "Ошибка подключения к локальному серверу.\n\n" +
                            "Проверьте:\n" +
                            "• Сервер запущен: python backend/server.py\n" +
                            "• URL сервера: $baseUrl\n" +
                            if (!isEmulator) {
                                "• IP компьютера: $expectedIp\n" +
                                "• Устройство и компьютер в одной Wi-Fi сети\n" +
                                "• Брандмауэр Windows разрешает входящие соединения на порт 8000\n" +
                                "• Тип устройства: Реальное устройство"
                            } else {
                                "• Тип устройства: Эмулятор\n" +
                                "• Для эмулятора используйте: http://10.0.2.2:8000/v1/"
                            }
                        } else {
                            "Ошибка подключения к серверу.\n\n" +
                            "Проверьте:\n" +
                            "• Сервер запущен и доступен\n" +
                            "• URL сервера: $baseUrl\n" +
                            "• Подключение к интернету"
                        }
                        detailedMessage
                    }
                }
            }
            
            is SocketTimeoutException -> {
                val baseUrl = if (com.example.bteu_schedule.data.config.AppConfig.USE_LOCAL_SERVER) {
                    if (com.example.bteu_schedule.data.config.AppConfig.isEmulator()) {
                        com.example.bteu_schedule.data.config.AppConfig.LOCAL_SERVER_URL_EMULATOR
                    } else {
                        com.example.bteu_schedule.data.config.AppConfig.LOCAL_SERVER_URL_DEVICE
                    }
                } else {
                    com.example.bteu_schedule.data.config.AppConfig.BASE_URL
                }
                "Превышено время ожидания ответа от сервера.\n\n" +
                "Проверьте:\n" +
                "• Сервер запущен: python backend/server.py\n" +
                "• URL сервера: $baseUrl\n" +
                "• Подключение к интернету\n" +
                "• Устройство и компьютер в одной Wi-Fi сети\n\n" +
                "Для эмулятора используйте: http://10.0.2.2:8000/v1/\n" +
                "Для реального устройства: http://[IP_компьютера]:8000/v1/"
            }
            
            is UnknownHostException -> {
                "Не удалось найти сервер.\n" +
                "Проверьте подключение к интернету и настройки сервера."
            }
            
            is IOException -> {
                val message = exception.message ?: ""
                val baseUrl = if (com.example.bteu_schedule.data.config.AppConfig.USE_LOCAL_SERVER) {
                    if (com.example.bteu_schedule.data.config.AppConfig.isEmulator()) {
                        com.example.bteu_schedule.data.config.AppConfig.LOCAL_SERVER_URL_EMULATOR
                    } else {
                        com.example.bteu_schedule.data.config.AppConfig.LOCAL_SERVER_URL_DEVICE
                    }
                } else {
                    com.example.bteu_schedule.data.config.AppConfig.BASE_URL
                }
                when {
                    message.contains("failed to connect") || message.contains("Unable to resolve host") -> {
                        val isEmulator = com.example.bteu_schedule.data.config.AppConfig.isEmulator()
                        val expectedIp = if (isEmulator) "10.0.2.2" else "192.168.1.25"
                        if (!isEmulator) {
                            "Не удалось подключиться к локальному серверу.\n\n" +
                            "Убедитесь, что:\n" +
                            "• Сервер запущен: python backend/server.py\n" +
                            "• URL сервера: $baseUrl\n" +
                            "• IP компьютера: $expectedIp\n" +
                            "• Устройство и компьютер в одной Wi-Fi сети\n" +
                            "• Брандмауэр Windows разрешает входящие соединения на порт 8000\n" +
                            "• Тип устройства: Реальное устройство\n\n" +
                            "Проверьте: откройте в браузере на устройстве http://$expectedIp:8000/v1/health"
                        } else {
                            "Не удалось подключиться к локальному серверу.\n\n" +
                            "Убедитесь, что:\n" +
                            "• Сервер запущен: python backend/server.py\n" +
                            "• URL сервера: $baseUrl\n" +
                            "• Тип устройства: Эмулятор\n" +
                            "• Для эмулятора используйте: http://10.0.2.2:8000/v1/"
                        }
                    }
                    message.contains("timeout") -> {
                        "Превышено время ожидания подключения.\n\n" +
                        "Проверьте:\n" +
                        "• Сервер запущен и доступен\n" +
                        "• URL сервера: $baseUrl\n" +
                        "• Устройство и компьютер в одной Wi-Fi сети"
                    }
                    else -> {
                        "Ошибка сети: ${exception.message}\n\n" +
                        "Проверьте:\n" +
                        "• Сервер запущен: python backend/server.py\n" +
                        "• URL сервера: $baseUrl\n" +
                        "• Подключение к интернету"
                    }
                }
            }
            
            else -> {
                val message = exception.message ?: "Неизвестная ошибка"
                when {
                    message.contains("timeout", ignoreCase = true) -> {
                        "Превышено время ожидания ответа от сервера.\n\n" +
                        "Приложение автоматически попыталось повторить запрос несколько раз.\n" +
                        "Проверьте подключение к интернету."
                    }
                    message.contains("unable to resolve host", ignoreCase = true) ||
                    message.contains("failed to connect", ignoreCase = true) -> {
                        "Не удалось подключиться к серверу.\n\n" +
                        "Приложение автоматически попыталось повторить запрос.\n" +
                        "Проверьте:\n" +
                        "• Подключение к интернету\n" +
                        "• Доступность сервера"
                    }
                    else -> {
                        "Ошибка сети: $message\n\n" +
                        "Приложение автоматически попыталось повторить запрос.\n" +
                        "Если проблема сохраняется, проверьте подключение к интернету."
                    }
                }
            }
        }
    }
    
    /**
     * Форматирует HTTP код ошибки в понятное сообщение
     */
    fun formatHttpError(code: Int, message: String? = null): String {
        val httpMessage = when (code) {
            400 -> "Неверный запрос"
            401 -> "Требуется авторизация"
            403 -> "Доступ запрещен"
            404 -> "Данные не найдены"
            500 -> "Ошибка сервера"
            502 -> "Сервер временно недоступен"
            503 -> "Сервис временно недоступен"
            else -> "Ошибка сервера (код $code)"
        }
        
        return if (message != null && message.isNotBlank()) {
            "$httpMessage: $message"
        } else {
            httpMessage
        }
    }
    
    /**
     * Форматирует полное сообщение об ошибке с контекстом
     */
    fun formatFullError(
        errorType: String,
        details: String? = null,
        useLocalData: Boolean = true
    ): String {
        val baseMessage = when (errorType) {
            "network" -> "Ошибка сети"
            "server" -> "Ошибка сервера"
            "timeout" -> "Превышено время ожидания"
            "connection" -> "Ошибка подключения"
            else -> "Ошибка загрузки"
        }
        
        val detailsPart = if (details != null && details.isNotBlank()) {
            ": $details"
        } else {
            ""
        }
        
        val localDataPart = if (useLocalData) {
            "\nИспользуются локальные данные."
        } else {
            ""
        }
        
        return "$baseMessage$detailsPart$localDataPart"
    }
}

