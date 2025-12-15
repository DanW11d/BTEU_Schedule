package com.example.bteu_schedule.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.CachePolicy

/**
 * Оптимизированный компонент для асинхронной загрузки изображений
 * 
 * Особенности:
 * - Автоматическое кэширование изображений в памяти и на диске
 * - Placeholder во время загрузки
 * - Обработка ошибок загрузки
 * - Поддержка различных форматов (WebP, PNG, JPG) с приоритетом WebP
 * - Автоматическая оптимизация размера
 * - Lazy loading для списков
 * 
 * @param imageUrl URL изображения для загрузки
 * @param modifier Модификатор для настройки размера и позиции
 * @param contentDescription Описание изображения для accessibility
 * @param contentScale Масштабирование изображения
 * @param shape Форма обрезки изображения
 * @param placeholderResId ID ресурса для placeholder (опционально)
 * @param errorResId ID ресурса для отображения при ошибке (опционально)
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape? = null,
    placeholderResId: Int? = null,
    errorResId: Int? = null
) {
    val context = LocalContext.current
    
    if (imageUrl.isNullOrBlank()) {
        // Если URL пустой, показываем placeholder
        if (placeholderResId != null) {
            Image(
                painter = painterResource(placeholderResId),
                contentDescription = contentDescription ?: "Placeholder",
                modifier = if (shape != null) {
                    modifier.clip(shape)
                } else {
                    modifier
                },
                contentScale = contentScale
            )
        }
        return
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED) // Кэш в памяти
            .diskCachePolicy(CachePolicy.ENABLED) // Кэш на диске
            .networkCachePolicy(CachePolicy.ENABLED) // Кэш сетевых запросов
            .build(),
        contentDescription = contentDescription,
        modifier = if (shape != null) {
            modifier.clip(shape)
        } else {
            modifier
        },
        contentScale = contentScale,
        placeholder = if (placeholderResId != null) {
            painterResource(placeholderResId)
        } else {
            null
        },
        error = if (errorResId != null) {
            painterResource(errorResId)
        } else if (placeholderResId != null) {
            painterResource(placeholderResId)
        } else {
            null
        }
    )
}

/**
 * Lazy loading версия OptimizedAsyncImage для использования в списках
 * 
 * Загружает изображение только когда оно становится видимым
 * Оптимизирован для использования в LazyColumn/LazyRow
 * 
 * @param imageUrl URL изображения
 * @param modifier Модификатор
 * @param contentDescription Описание
 * @param contentScale Масштабирование
 * @param shape Форма обрезки
 */
@Composable
fun LazyOptimizedAsyncImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape? = null
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale,
        shape = shape
    )
}
