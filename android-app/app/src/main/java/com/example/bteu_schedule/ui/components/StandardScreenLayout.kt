package com.example.bteu_schedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.theme.DesignSpacing

/**
 * Общая структура экранов (Layout Pattern) — A3.5
 * 
 * Правила визуальной иерархии:
 * 1. Один главный заголовок на экран (без лишних надписей "Меню", "Список")
 * 2. Группировка по смыслу (каждый блок читается как единая группа)
 * 3. Пространство между блоками: минимум 16dp, лучше 20dp
 * 4. Избегать визуального шума:
 *    - Градиенты только для Primary карточек
 *    - Минимум рамок
 *    - Мягкие тени (DesignShadows.Low)
 * 
 * Правило построения экрана:
 * 1. Хедер (градиент + fade)
 * 2. Title + Subtitle (опционально, если не в хедере)
 * 3. Ввод/Фильтр (опционально)
 * 4. Основной контент (группировка, отступы 16dp между блоками)
 * 5. Нижняя навигация (опционально)
 * 
 * Пример структуры:
 * - Header (градиент + fade)
 * - Title + Subtitle
 * - (Поиск/фильтры — если нужны)
 * - Основные карточки (отступы 16dp между блоками)
 * - Таббар
 * 
 * Пример использования:
 * ```
 * StandardScreenLayout(
 *     title = "Расписание", // Один главный заголовок
 *     subtitle = "Группа ПИ-21-1",
 *     showSearch = true,
 *     searchContent = { SearchBar(...) },
 *     showBottomNavigation = true,
 *     bottomNavigationContent = { CustomBottomNavigation(...) }
 * ) {
 *     // Основной контент (группировка, отступы 16dp)
 *     PrimaryCard(...) // Градиент ✅
 *     PrimaryCard(...) // Градиент ✅
 *     ListCard(...)    // Однотонный фон ✅
 *     ListCard(...)    // Однотонный фон ✅
 * }
 * ```
 */
@Composable
fun StandardScreenLayout(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    showHeader: Boolean = true,
    currentGroup: GroupUi? = null,
    onProfileClick: (() -> Unit)? = null,
    showSearch: Boolean = false,
    searchContent: @Composable (() -> Unit)? = null,
    showFilters: Boolean = false,
    filterContent: @Composable (() -> Unit)? = null,
    showBottomNavigation: Boolean = false,
    bottomNavigationContent: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val listState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Хедер (градиент + fade)
            if (showHeader) {
                AdaptiveHeader(
                    title = title,
                    subtitle = subtitle,
                    isVisible = true,
                    isScrolled = isScrolled.value,
                    currentGroup = currentGroup,
                    onProfileClick = onProfileClick
                )
            } else {
                // Если хедер скрыт, но нужен Title + Subtitle
                if (subtitle != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + DesignSpacing.FromHeader,
                                start = DesignSpacing.Base,
                                end = DesignSpacing.Base
                            )
                    ) {
                        androidx.compose.material3.Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle != null) {
                            Spacer(modifier = Modifier.height(DesignSpacing.XS))
                            androidx.compose.material3.Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Fade-переход удален: контент начинается сразу после хедера (как на экране "Выберите форму обучения")

            // 3. Ввод/Фильтр (опционально)
            if (showSearch || showFilters) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = DesignSpacing.Base,
                            vertical = DesignSpacing.M
                        )
                ) {
                    if (showSearch && searchContent != null) {
                        searchContent()
                        if (showFilters) {
                            Spacer(modifier = Modifier.height(DesignSpacing.S))
                        }
                    }
                    if (showFilters && filterContent != null) {
                        filterContent()
                    }
                }
            }

            // 4. Основной контент
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            val bottomNavHeight = if (showBottomNavigation) 100.dp else 0.dp

            // Белый блок контента со скруглением 24dp сверху
            // Лёгкая тень и мягкий fade сверху
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(
                            topStart = 24.dp, // Скругление верх белой области (24dp)
                            topEnd = 24.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .shadow(
                        elevation = 4.dp, // Лёгкая тень
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        ),
                        spotColor = Color.Black.copy(alpha = 0.05f) // Мягкая тень (5%)
                    ),
                contentPadding = PaddingValues(
                    top = if (showSearch || showFilters) {
                        DesignSpacing.S
                    } else {
                        if (showHeader) {
                            24.dp // Отступ после белого блока (20-32dp, используем 24dp)
                        } else {
                            DesignSpacing.FromHeader
                        }
                    },
                    bottom = bottomNavHeight + navigationBarsPadding.calculateBottomPadding(),
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base
                ),
                // A3.5: Пространство между блоками - минимум 16dp (идеал 20dp)
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.BetweenBlocks) // 16dp между блоками
            ) {
                item {
                    content()
                }
            }
        }

        // 5. Нижняя навигация (опционально)
        if (showBottomNavigation && bottomNavigationContent != null) {
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
            ) {
                bottomNavigationContent()
            }
        }
    }
}

/**
 * Упрощённая версия для экранов без поиска и фильтров
 */
@Composable
fun StandardScreenLayout(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    showHeader: Boolean = true,
    currentGroup: GroupUi? = null,
    onProfileClick: (() -> Unit)? = null,
    showBottomNavigation: Boolean = false,
    bottomNavigationContent: @Composable (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    val isScrolled = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Хедер (градиент + fade)
            if (showHeader) {
                AdaptiveHeader(
                    title = title,
                    subtitle = subtitle,
                    isVisible = true,
                    isScrolled = isScrolled.value,
                    currentGroup = currentGroup,
                    onProfileClick = onProfileClick
                )
            }

            // 2. Fade-переход удален: контент начинается сразу после хедера (как на экране "Выберите форму обучения")

            // 3. Основной контент
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            val bottomNavHeight = if (showBottomNavigation) 100.dp else 0.dp

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .clip(RoundedCornerShape(0.dp)),
                contentPadding = PaddingValues(
                    top = DesignSpacing.FromHeader,
                    bottom = bottomNavHeight + navigationBarsPadding.calculateBottomPadding(),
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base
                ),
                // A3.5: Пространство между блоками - минимум 16dp (идеал 20dp)
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.BetweenBlocks) // 16dp между блоками
            ) {
                content()
            }
        }

        // 4. Нижняя навигация (опционально)
        if (showBottomNavigation && bottomNavigationContent != null) {
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
            ) {
                bottomNavigationContent()
            }
        }
    }
}

