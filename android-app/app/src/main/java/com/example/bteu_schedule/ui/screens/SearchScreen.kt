package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.LessonUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.ScreenUtils
import com.example.bteu_schedule.ui.utils.premiumChipPressAnimation
import com.example.bteu_schedule.ui.utils.premiumCardAppearAnimation
import com.example.bteu_schedule.ui.viewmodel.SearchUiState
import com.example.bteu_schedule.ui.viewmodel.SearchViewModel
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

enum class SearchFilterType {
    ALL, SUBJECT, TEACHER, CLASSROOM
}

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    currentGroup: GroupUi? = null,
    onProfileClick: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilterType.ALL) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Анимация появления экрана
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isScreenVisible = true
    }

    LaunchedEffect(searchQuery) {
        viewModel.onSearchQueryChange(searchQuery, currentGroup?.code)
    }

    LaunchedEffect(selectedFilter) {
        viewModel.onFilterChange(selectedFilter)
    }

    val colors = designColors()
    val listState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }
    
    // ✅ Используем MaterialTheme.colorScheme.background вместо градиента для единообразия
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Контент без statusBarsPadding - хедер сам включает статус-бар
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Адаптивный хедер с фиксированной высотой и единым градиентом
            AdaptiveHeader(
                title = "Поиск",
                subtitle = null,
                isVisible = isScreenVisible,
                isScrolled = isScrolled.value,
                currentGroup = currentGroup,
                onProfileClick = onProfileClick
            )
            
            // Поисковая строка и фильтры под хедером - начинаются сразу после градиента
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        horizontal = DesignSpacing.Base,
                        vertical = DesignSpacing.M
                    ),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                
                FilterChipsRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Контент поиска - начинается сразу после хедера
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                SearchContent(
                    uiState = uiState,
                    searchQuery = searchQuery,
                    listState = listState
                )
            }
        }
    }
}

@Composable
private fun SearchContent(
    uiState: SearchUiState,
    searchQuery: String,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState()
) {
    AnimatedVisibility(
        visible = uiState.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    if (!uiState.isLoading) {
        when {
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSpacing.Base),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.S)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(DesignIconSizes.Large * 1.5f)
                        )
                        Text(
                            uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            searchQuery.isBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSpacing.Base),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(DesignRadius.M),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSpacing.XL * 1.33f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(DesignIconSizes.Large * 2)
                            )
                            Text(
                                "ВВЕДИТЕ ЗАПРОС",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Введите запрос для поиска по расписанию",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            uiState.filteredResults.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSpacing.Base),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(DesignRadius.M),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSpacing.XL * 1.33f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(DesignIconSizes.Large * 2)
                            )
                            Text(
                                "НИЧЕГО НЕ НАЙДЕНО",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Попробуйте изменить запрос или фильтр",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            else -> {
                val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = DesignSpacing.M,
                        start = DesignSpacing.Base,
                        end = DesignSpacing.Base,
                        bottom = DesignSpacing.XL + navigationBarsPadding.calculateBottomPadding() + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                ) {
                    item {
                        Text(
                            text = "Найдено: ${uiState.filteredResults.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = DesignSpacing.XS)
                        )
                    }
                    itemsIndexed(uiState.filteredResults) { index, lesson ->
                        AnimatedVisibility(
                            visible = true,
                            enter = premiumCardAppearAnimation(index),
                            exit = fadeOut()
                        ) {
                            SearchResultCard(lesson = lesson)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp, // ✅ Уменьшено для единообразия
                shape = RoundedCornerShape(DesignRadius.M),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(DesignRadius.M)),
        placeholder = {
            Text(
                "Поиск по расписанию...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Поиск",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(DesignIconSizes.Medium)
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        "Очистить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(DesignIconSizes.Medium)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true,
        shape = RoundedCornerShape(DesignRadius.M)
    )
}

@Composable
fun FilterChipsRow(
    selectedFilter: SearchFilterType,
    onFilterSelected: (SearchFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = designColors()
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S)
    ) {
        SearchFilterType.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            // Премиальная анимация чипа
            val chipAnimation = premiumChipPressAnimation(isPressed)
            
            // Плавная анимация цвета фона и текста
            // ✅ Исправление контрастности: неактивные фильтры должны быть видны на белом фоне
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) {
                    colors.primaryLight.copy(alpha = 0.15f) // Светлый фон для активного
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Светло-серый фон для неактивного
                },
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "chipContainerColor"
            )
            
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) {
                    colors.primaryLight // Синий текст для активного
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant // ✅ Темный текст для неактивного (виден на белом фоне)
                },
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                ),
                label = "chipLabelColor"
            )
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                interactionSource = interactionSource,
                label = {
                    Text(
                        text = when (filter) {
                            SearchFilterType.ALL -> "Все"
                            SearchFilterType.SUBJECT -> "Предмет"
                            SearchFilterType.TEACHER -> "Преподаватель"
                            SearchFilterType.CLASSROOM -> "Аудитория"
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = containerColor,
                    selectedLabelColor = labelColor,
                    containerColor = containerColor,
                    labelColor = labelColor
                ),
                border = null,
                shape = CircleShape,
                modifier = Modifier.scale(chipAnimation.scale)
            )
        }
    }
}

@Composable
fun SearchResultCard(lesson: LessonUi, modifier: Modifier = Modifier) {
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
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = { /* Navigate to lesson details */ },
                interactionSource = interactionSource,
                indication = null
            ),
        shape = RoundedCornerShape(DesignRadius.M),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(DesignSpacing.CardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
        ) {
            // Time Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(70.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = getDayName(lesson.dayOfWeek).take(3).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = lesson.time.split("-").firstOrNull() ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(50.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), 
                        RoundedCornerShape(1.dp)
                    )
            )
            
            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = lesson.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (lesson.teacher.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(DesignIconSizes.Small * 0.8f),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = lesson.teacher,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (lesson.classroom.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(DesignIconSizes.Small * 0.8f),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = lesson.classroom,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun getDayName(dayOfWeek: Int): String = when (dayOfWeek) {
    1 -> "Понедельник"; 2 -> "Вторник"; 3 -> "Среда"; 4 -> "Четверг"; 5 -> "Пятница"; 6 -> "Суббота"; else -> "День $dayOfWeek"
}