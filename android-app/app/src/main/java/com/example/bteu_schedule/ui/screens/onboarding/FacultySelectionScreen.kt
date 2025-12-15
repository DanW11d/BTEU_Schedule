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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.FacultyUi
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.viewmodel.FacultySelectionViewModel
import com.example.bteu_schedule.ui.viewmodel.FacultyUiState

/**
 * Карточка факультета в стиле приложения - светлая карточка с границей и тенью
 */
@Composable
fun FacultyCard(
    faculty: FacultyUi,
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
    val iconSize = DesignIconSizes.Medium
    val arrowIconSize = DesignIconSizes.Medium
    val rowSpacing = DesignSpacing.Base
    
    // Иконка для всех факультетов - здание/университет
    val icon = Icons.Default.AccountBalance
    
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
                    Box(
                        modifier = Modifier
                            .size(iconBoxSize)
                            .background(
                                color = colors.primaryLight.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(iconBoxCornerRadius)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = faculty.name,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS)
                    ) {
                        Text(
                            text = faculty.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (faculty.description.isNotBlank()) {
                            Text(
                                text = faculty.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

@Composable
fun FacultySelectionScreen(
    modifier: Modifier = Modifier,
    onFacultySelected: (FacultyUi) -> Unit,
    viewModel: FacultySelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
                title = "Выберите факультет",
                subtitle = "Выберите ваш факультет для продолжения",
                isVisible = true,
                isScrolled = isScrolled.value,
                headerType = HeaderType.NAVIGATION
            )
            
            when (val state = uiState) {
                is FacultyUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
                        ) {
                            CircularProgressIndicator(
                                color = colors.primaryLight
                            )
                            Text(
                                text = "Загрузка данных...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Это может занять несколько секунд",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is FacultyUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSpacing.Base),
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.loadFaculties() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(DesignHeights.Button),
                            shape = RoundedCornerShape(DesignRadius.M),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryLight
                            )
                        ) {
                            Text(
                                "Повторить попытку",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is FacultyUiState.Success -> {
                    // Список факультетов с поддержкой скролла
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
                        items(state.faculties, key = { it.id }) { faculty ->
                            FacultyCard(
                                faculty = faculty,
                                onClick = { onFacultySelected(faculty) }
                            )
                        }
                    }
                }
            }
        }
    }
}
