package com.example.bteu_schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.AppGradients

/**
 * Переиспользуемая карточка с градиентным фоном
 * 
 * @param modifier Модификатор для настройки размера и позиции
 * @param gradientColors Список цветов для градиента
 * @param onClick Обработчик клика
 * @param content Содержимое карточки
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    onClick: (() -> Unit)? = null,
    elevation: androidx.compose.ui.unit.Dp = 4.dp,
    shape: androidx.compose.foundation.shape.RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = AppGradients.horizontalGradient(gradientColors),
                    shape = shape
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * Переиспользуемая карточка с градиентным фоном и иконкой
 * 
 * @param modifier Модификатор для настройки размера и позиции
 * @param gradientColors Список цветов для градиента
 * @param icon Иконка для отображения
 * @param title Заголовок карточки
 * @param subtitle Подзаголовок (опционально)
 * @param onClick Обработчик клика
 */
@Composable
fun GradientIconCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    elevation: androidx.compose.ui.unit.Dp = 4.dp,
    shape: androidx.compose.foundation.shape.RoundedCornerShape = RoundedCornerShape(16.dp)
) {
    GradientCard(
        modifier = modifier,
        gradientColors = gradientColors,
        onClick = onClick,
        elevation = elevation,
        shape = shape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка с градиентным фоном
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = AppGradients.horizontalGradient(gradientColors),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Текст
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Стрелка вперед
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Перейти",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

