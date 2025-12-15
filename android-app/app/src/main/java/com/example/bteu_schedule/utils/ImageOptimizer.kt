package com.example.bteu_schedule.utils

import android.graphics.Bitmap
import com.example.bteu_schedule.data.config.AppConstants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Утилита для оптимизации изображений
 * 
 * Предоставляет методы для:
 * - Конвертации изображений в WebP формат
 * - Сжатия изображений
 * - Оптимизации размера
 */
object ImageOptimizer {

    /**
     * Максимальная ширина изображения для оптимизации (в пикселях)
     */
    private const val MAX_IMAGE_WIDTH = AppConstants.IMAGE_MAX_WIDTH

    /**
     * Максимальная высота изображения для оптимизации (в пикселях)
     */
    private const val MAX_IMAGE_HEIGHT = AppConstants.IMAGE_MAX_HEIGHT

    /**
     * Качество сжатия WebP (0-100, где 100 - лучшее качество)
     */
    private const val WEBP_QUALITY = AppConstants.IMAGE_WEBP_QUALITY

    /**
     * Проверить, нужно ли оптимизировать изображение
     * 
     * @param width Ширина изображения
     * @param height Высота изображения
     * @return true, если изображение превышает максимальные размеры
     */
    fun shouldOptimize(width: Int, height: Int): Boolean {
        return width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT
    }

    /**
     * Вычислить оптимальный размер изображения с сохранением пропорций
     * 
     * @param originalWidth Исходная ширина
     * @param originalHeight Исходная высота
     * @return Пара (ширина, высота) для оптимального размера
     */
    fun calculateOptimalSize(originalWidth: Int, originalHeight: Int): Pair<Int, Int> {
        if (!shouldOptimize(originalWidth, originalHeight)) {
            return Pair(originalWidth, originalHeight)
        }

        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        return when {
            originalWidth > originalHeight -> {
                // Горизонтальное изображение
                Pair(MAX_IMAGE_WIDTH, (MAX_IMAGE_WIDTH / aspectRatio).toInt())
            }
            else -> {
                // Вертикальное или квадратное изображение
                Pair((MAX_IMAGE_HEIGHT * aspectRatio).toInt(), MAX_IMAGE_HEIGHT)
            }
        }
    }

    /**
     * Оптимизировать размер изображения
     * 
     * @param bitmap Исходное изображение
     * @return Оптимизированное изображение
     */
    fun optimizeBitmapSize(bitmap: Bitmap): Bitmap {
        val (optimalWidth, optimalHeight) = calculateOptimalSize(bitmap.width, bitmap.height)
        
        if (bitmap.width == optimalWidth && bitmap.height == optimalHeight) {
            return bitmap // Размер уже оптимальный
        }

        return Bitmap.createScaledBitmap(bitmap, optimalWidth, optimalHeight, true)
    }

    /**
     * Сохранить изображение в WebP формате
     * 
     * @param bitmap Изображение для сохранения
     * @param outputFile Файл для сохранения
     * @param quality Качество сжатия (0-100)
     * @return true, если сохранение успешно
     */
    fun saveAsWebP(bitmap: Bitmap, outputFile: File, quality: Int = WEBP_QUALITY): Boolean {
        return try {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.WEBP, quality, out)
            }
            true
        } catch (e: IOException) {
            AppLogger.e("ImageOptimizer", "Ошибка сохранения изображения в WebP", e)
            false
        }
    }

    /**
     * Получить размер файла изображения в формате строки
     * 
     * @param file Файл изображения
     * @return Размер в формате "X KB" или "X MB"
     */
    fun getFileSizeString(file: File): String {
        val sizeBytes = file.length()
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> "${sizeBytes / (1024 * 1024)} MB"
        }
    }

    /**
     * Получить размер файла изображения в байтах
     * 
     * @param file Файл изображения
     * @return Размер в байтах
     */
    fun getFileSizeBytes(file: File): Long {
        return file.length()
    }

    /**
     * Проверить, является ли файл изображением WebP
     * 
     * @param file Файл для проверки
     * @return true, если файл в формате WebP
     */
    fun isWebPFormat(file: File): Boolean {
        return file.extension.equals("webp", ignoreCase = true)
    }

    /**
     * Проверить, нужно ли конвертировать изображение в WebP
     * 
     * @param file Файл изображения
     * @return true, если файл не в формате WebP
     */
    fun shouldConvertToWebP(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("png", "jpg", "jpeg") && !isWebPFormat(file)
    }
}

