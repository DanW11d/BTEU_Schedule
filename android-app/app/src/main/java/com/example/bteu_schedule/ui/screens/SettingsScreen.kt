package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.ThemeManager
import com.example.bteu_schedule.ui.theme.ThemeMode
import com.example.bteu_schedule.ui.theme.LanguageManager
import com.example.bteu_schedule.ui.theme.LanguageMode
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.ScreenUtils
import com.example.bteu_schedule.ui.utils.premiumCardAppearAnimation
import com.example.bteu_schedule.viewmodel.SettingsViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.WindowInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// Используем designColors() для получения акцентных цветов

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    themeManager: ThemeManager,
    languageManager: LanguageManager,
    currentGroup: GroupUi?,
    onProfileClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    onGroupChangeClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onOpenAiSettingsClick: () -> Unit,
    onClearCacheClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var isScreenVisible by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50) // Небольшая задержка для более плавного перехода
        isScreenVisible = true
    }

    val listState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50 }
    }

    // Полноэкранный режим: фон идет на весь экран
    // Градиент хедера плавно переходит в контент (как на главном экране)
    val colors = designColors()
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
        // Контент без statusBarsPadding - хедер сам включает статус-бар
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Адаптивный хедер с фиксированной высотой и единым градиентом
            AdaptiveHeader(
                title = "Настройки",
                subtitle = null,
                isVisible = isScreenVisible,
                isScrolled = isScrolled.value,
                currentGroup = currentGroup,
                onProfileClick = onProfileClick
            )

            // Скроллируемый контент - начинается сразу после градиента хедера
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            val bottomNavigationHeight = 100.dp // Высота нижней навигации
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    top = DesignSpacing.M,
                    bottom = DesignSpacing.XL + bottomNavigationHeight + navigationBarsPadding.calculateBottomPadding(),
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base
                ),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
            ) {
                // Секция "Внешний вид"
                item {
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = premiumCardAppearAnimation(0),
                        exit = fadeOut()
                    ) {
                        SettingsThemedCard(
                            title = "Внешний вид",
                            icon = Icons.Default.Palette,
                        ) {
                            ThemeSelector(themeManager = themeManager)
                        }
                    }
                }

                // Секция "Язык интерфейса"
                item {
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = premiumCardAppearAnimation(1),
                        exit = fadeOut()
                    ) {
                        SettingsThemedCard(
                            title = "Язык интерфейса",
                            icon = Icons.Default.Language,
                        ) {
                            LanguageSelector(languageManager = languageManager)
                        }
                    }
                }

                // Список остальных настроек
                val settingsItems = listOf(
                    SettingsItemData("Уведомления", Icons.Default.Notifications, onNotificationSettingsClick),
                    SettingsItemData("ChatGPT API", Icons.Default.Psychology, onOpenAiSettingsClick),
                    SettingsItemData("Очистить кэш", Icons.Default.Delete, { showClearCacheDialog = true }),
                    SettingsItemData("О приложении", Icons.Default.Info, onAboutClick),
                )

                itemsIndexed(settingsItems) { index, item ->
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = premiumCardAppearAnimation(index + 2),
                        exit = fadeOut()
                    ) {
                        NavigationItemCard(item = item)
                    }
                }

                // Кнопка выхода
                item {
                    AnimatedVisibility(
                        visible = isScreenVisible,
                        enter = premiumCardAppearAnimation(settingsItems.size + 1),
                        exit = fadeOut()
                    ) {
                        LogoutButton(onClick = onLogoutClick)
                    }
                }
            }
        }
        
        // Диалог подтверждения очистки кэша
        if (showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearCacheDialog = false },
                title = {
                    Text(
                        text = "Очистить кэш",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("Вы уверены, что хотите очистить кэш FTP файлов? Все скачанные файлы будут удалены и загружены заново при следующей синхронизации.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearCacheDialog = false
                            onClearCacheClick()
                        }
                    ) {
                        Text("Очистить", color = colors.primaryLight)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearCacheDialog = false }
                    ) {
                        Text("Отмена")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
        
    }
}

private data class SettingsItemData(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
private fun SettingsThemedCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = designColors()
    val cornerRadius = DesignRadius.L // 24px для больших карточек

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = colors.primaryLight.copy(alpha = 0.15f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(DesignSpacing.CardPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.CardInternalSpacing)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(DesignIconSizes.Medium)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                content()
            }
        }
    }
}

@Composable
private fun ThemeSelector(themeManager: ThemeManager) {
    val colors = designColors()
    val currentTheme by themeManager.themeMode.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
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

    val currentThemeText = when (currentTheme) {
        ThemeMode.LIGHT -> "Светлая"
        ThemeMode.DARK -> "Темная"
        ThemeMode.SYSTEM -> "Системная"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = { showDialog = true },
                interactionSource = interactionSource,
                indication = null
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = colors.primaryLight.copy(alpha = 0.1f)
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
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Тема",
                            tint = colors.primaryLight,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Text(
                        text = currentThemeText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

    // Диалог выбора темы
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Выберите тему",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.S)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = mode == currentTheme
                        val (icon, label) = when (mode) {
                            ThemeMode.LIGHT -> Pair(Icons.Default.WbSunny, "Светлая")
                            ThemeMode.DARK -> Pair(Icons.Default.Brightness2, "Темная")
                            ThemeMode.SYSTEM -> Pair(Icons.Default.PhoneAndroid, "Системная")
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    themeManager.updateThemeMode(mode)
                                    showDialog = false
                                }
                                .padding(DesignSpacing.M),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                ) {
                    Icon(
                        imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) colors.primaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(DesignIconSizes.Medium)
                    )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.primaryLight else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(DesignIconSizes.Medium)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LanguageSelector(languageManager: LanguageManager) {
    val colors = designColors()
    val currentLanguage by languageManager.languageMode.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

    val currentLanguageText = when (currentLanguage) {
        LanguageMode.RUSSIAN -> "Русский"
        LanguageMode.BELARUSIAN -> "Беларуская"
        LanguageMode.ENGLISH -> "English"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = { showDialog = true },
                interactionSource = interactionSource,
                indication = null
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = colors.primaryLight.copy(alpha = 0.1f)
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
                            imageVector = Icons.Default.Language,
                            contentDescription = "Язык",
                            tint = colors.primaryLight,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Text(
                        text = currentLanguageText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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

    // Диалог выбора языка
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Выберите язык",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.S)
                ) {
                    LanguageMode.entries.forEach { mode ->
                        val isSelected = mode == currentLanguage
                        val label = when (mode) {
                            LanguageMode.RUSSIAN -> "Русский"
                            LanguageMode.BELARUSIAN -> "Беларуская"
                            LanguageMode.ENGLISH -> "English"
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (mode != currentLanguage) {
                                        try {
                                            languageManager.updateLanguageMode(mode)
                                            // Перезапускаем Activity для применения нового языка
                                            coroutineScope.launch {
                                                delay(200)
                                                (context as? ComponentActivity)?.recreate()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("LanguageSelector", "Ошибка при смене языка", e)
                                        }
                                    }
                                    showDialog = false
                                }
                                .padding(DesignSpacing.M),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = label,
                                tint = if (isSelected) colors.primaryLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(DesignIconSizes.Medium)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.primaryLight else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.primaryLight,
                                    modifier = Modifier.size(DesignIconSizes.Medium)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NavigationItemCard(item: SettingsItemData) {
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
    
    val cornerRadius = DesignRadius.M // 16px для карточек
    val cardPadding = DesignSpacing.CardPadding
    val iconBoxSize = DesignIconSizes.IconButtonSize * 0.83f // ~40dp
    val iconBoxCornerRadius = DesignRadius.XS // 8dp для мелких элементов
    val iconSize = DesignIconSizes.Medium
    val arrowIconSize = DesignIconSizes.Medium
    val rowSpacing = DesignSpacing.Base

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = item.onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = colors.primaryLight.copy(alpha = 0.1f)
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
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = colors.primaryLight,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Перейти",
                    tint = colors.primaryLight.copy(alpha = 0.6f),
                    modifier = Modifier.size(arrowIconSize)
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // ✅ Используем colorSurface с красным акцентом вместо хардкоженных цветов
    val dangerText = MaterialTheme.colorScheme.error // Красный цвет из темы
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "buttonPress"
    )
    
    val cornerRadius = DesignRadius.M // 16dp для кнопок
    val buttonHeight = DesignHeights.Button // 48dp высота
    val iconSize = DesignIconSizes.Medium
    val spacerWidth = DesignSpacing.M

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .shadow(
                elevation = 2.dp, // ✅ Уменьшено для менее агрессивного вида
                shape = RoundedCornerShape(cornerRadius),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
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
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.surface, // ✅ Используем colorSurface
                    shape = RoundedCornerShape(cornerRadius)
                )
                .border(
                    width = 1.dp,
                    color = dangerText.copy(alpha = 0.2f), // ✅ Тонкая красная граница
                    shape = RoundedCornerShape(cornerRadius)
                )
                .then(
                    if (isPressed) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Выйти",
                tint = dangerText,
                modifier = Modifier.size(iconSize)
            )
            Spacer(Modifier.width(spacerWidth))
            Text(
                text = "Выйти из аккаунта",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = dangerText
            )
        }
        }
    }
}