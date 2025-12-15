package com.example.bteu_schedule.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType

/**
 * Карточка курса в стиле приложения - светлая карточка с границей и тенью
 */
@Composable
fun CourseCard(
    course: Int,
    onClick: () -> Unit
) {
    val colors = designColors()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "cardPress"
    )
    
    val cornerRadius = DesignRadius.M
    val cardPadding = DesignSpacing.CardPadding
    val iconBoxSize = DesignIconSizes.IconButtonSize * 0.83f
    val iconBoxCornerRadius = DesignRadius.XS
    val arrowIconSize = DesignIconSizes.Medium
    val rowSpacing = DesignSpacing.Base
    
    val courseTitle = when (course) {
        1 -> "Первый курс"
        2 -> "Второй курс"
        3 -> "Третий курс"
        4 -> "Четвертый курс"
        else -> ""
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .applyShadow(
                shadowSpec = DesignShadows.Low,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 1.dp,
                color = Color(0x0F0D1333), // rgba(13,19,51,0.06)
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                    modifier = Modifier.weight(1f)
                ) {
                    // Круг с числом курса
                    Box(
                        modifier = Modifier
                            .size(iconBoxSize)
                            .background(
                                color = colors.primaryLight.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = course.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryLight
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS)
                    ) {
                        Text(
                            text = "$course курс",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = courseTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Выбрать",
                    tint = colors.primaryLight.copy(alpha = 0.6f),
                    modifier = Modifier.size(arrowIconSize)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onCourseSelected: (Int) -> Unit
) {
    // Курсы от 1 до 4
    val courses = listOf(1, 2, 3, 4)
    val colors = designColors()
    
    // Состояние скролла для анимации
    val scrollState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 50
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.primaryGradientStart,
                        colors.primaryGradientMid,
                        colors.primaryGradientEnd,
                        colors.bg
                    ),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Адаптивный хедер с единым градиентом
            AdaptiveHeader(
                title = "Выберите курс",
                subtitle = "Выберите ваш курс обучения",
                isVisible = true,
                isScrolled = isScrolled.value,
                backButton = true,
                onBackClick = onBack,
                headerType = HeaderType.NAVIGATION
            )
            
            // Список курсов
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base,
                    top = DesignSpacing.M,
                    bottom = DesignSpacing.XL
                ),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
            ) {
                items(courses) { course ->
                    CourseCard(
                        course = course,
                        onClick = { onCourseSelected(course) }
                    )
                }
            }
        }
    }
}
