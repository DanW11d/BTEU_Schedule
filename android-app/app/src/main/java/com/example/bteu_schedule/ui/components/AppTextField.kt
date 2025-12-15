package com.example.bteu_schedule.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.applyShadow

/**
 * AppTextField — UI-KIT компонент
 * 
 * Особенности:
 * - Высота: 48dp
 * - Радиус: 16dp
 * - Иконка слева
 * - Action справа (отправка сообщения, поиск)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingAction: ImageVector? = null,
    onTrailingActionClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    val shadowSpec = DesignShadows.Low
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DesignHeights.SearchField) // 48dp
            .applyShadow(
                shadowSpec = shadowSpec,
                shape = RoundedCornerShape(DesignRadius.M) // 16dp
            )
            .clip(RoundedCornerShape(DesignRadius.M))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge, // Body: 14sp Regular
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    // A3.4 - минимальный размер иконки 20dp
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp) // Минимальный размер: 20dp
                    )
                }
            } else null,
            trailingIcon = if (trailingAction != null && onTrailingActionClick != null) {
                {
                    // A3.3: IconButton автоматически 48dp, но явно указываем для ясности
                    IconButton(
                        onClick = onTrailingActionClick,
                        modifier = Modifier.size(48.dp) // Минимум 48dp для кликабельной зоны
                    ) {
                        // A3.4 - минимальный размер иконки 20dp
                        Icon(
                            imageVector = trailingAction,
                            contentDescription = "Действие",
                            tint = if (value.isNotEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color(0xFF9CA3AF) // Неактивные: #9CA3AF
                            },
                            modifier = Modifier.size(20.dp) // Минимальный размер: 20dp
                        )
                    }
                }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                // Элементы управления: 14-16sp Medium
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                // Placeholder: достаточный контраст (не светло-серый на светлом фоне)
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium // Medium для элементов управления
            ),
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(DesignRadius.M) // 16dp
        )
    }
}

