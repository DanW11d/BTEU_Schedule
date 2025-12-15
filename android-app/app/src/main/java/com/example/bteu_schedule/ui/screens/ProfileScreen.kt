package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.premiumCardAppearAnimation
import kotlinx.coroutines.delay

/**
 * Обновленный экран профиля студента
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    currentGroup: GroupUi?,
    facultyName: String?,
    departmentName: String?,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSelectGroupClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50) // Небольшая задержка для плавного перехода
        isVisible = true
    }

    val colors = designColors()
    
    // Полноэкранный режим: фон идет на весь экран
    // Градиент хедера плавно переходит в контент (как на других экранах)
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
                title = "Профиль студента",
                subtitle = if (currentGroup != null) "Информация о группе" else "Настройка профиля",
                isVisible = true,
                isScrolled = false,
                backButton = true,
                onBackClick = onBack,
                headerType = HeaderType.NAVIGATION
            )

            // Контент начинается сразу после хедера (как в HomeScreen)
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            val bottomNavigationHeight = DesignHeights.BottomNavigation
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background), // Белый фон как в HomeScreen
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    start = DesignSpacing.Base, // Design Tokens: 16dp
                    end = DesignSpacing.Base, // Design Tokens: 16dp
                    top = DesignSpacing.M, // Design Tokens: 12dp отступ сверху
                    bottom = DesignSpacing.XL + bottomNavigationHeight + navigationBarsPadding.calculateBottomPadding() + DesignSpacing.L
                ),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.L) // Design Tokens: 20dp как в HomeScreen
            ) {
            // Приветственная карточка с группой (как ModernWelcomeCard в HomeScreen)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(600)) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    if (currentGroup != null) {
                        ModernProfileWelcomeCard(group = currentGroup)
                    } else {
                        ProfileNotSetHeader()
                    }
                }
            }

            if (currentGroup != null) {
                // Карточка с информацией о группе (стиль ModernGradientCard)
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = premiumCardAppearAnimation(0),
                        exit = fadeOut()
                    ) {
                        ModernInfoCard(
                            title = "Информация о группе",
                            icon = Icons.Default.Info,
                            gradientColors = listOf(colors.classicBlueStart, colors.classicBlueEnd),
                            content = {
                                InfoRow(label = "Специальность", value = currentGroup.specialization)
                                InfoRow(label = "Курс", value = "${currentGroup.course} курс")
                                InfoRow(label = "Форма обучения", value = getEducationFormRu(currentGroup.educationForm))
                            }
                        )
                    }
                }

                // Карточка с информацией о факультете
                item {
                    // Пробуем получить название факультета из переданного параметра или по коду
                    val facultyDisplayName = when {
                        !facultyName.isNullOrBlank() && !facultyName.matches(Regex("^\\d+$")) -> facultyName
                        !currentGroup.facultyCode.isNullOrBlank() -> {
                            getFacultyNameByCode(currentGroup.facultyCode) 
                                ?: currentGroup.facultyCode.takeIf { !it.matches(Regex("^\\d+$")) }
                                ?: ""
                        }
                        else -> ""
                    }
                    
                    if (facultyDisplayName.isNotBlank()) {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = premiumCardAppearAnimation(1),
                            exit = fadeOut()
                        ) {
                            ModernInfoCard(
                                title = "Подразделение",
                                icon = Icons.Default.AccountBalance,
                                gradientColors = listOf(colors.brightBlueStart, colors.brightBlueEnd),
                                content = {
                                    InfoRow(label = "Факультет", value = facultyDisplayName)
                                    if (!departmentName.isNullOrBlank()) {
                                        InfoRow(label = "Кафедра", value = departmentName)
                                    }
                                }
                            )
                        }
                    }
                }

                // Кнопки действий
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = premiumCardAppearAnimation(2),
                        exit = fadeOut()
                    ) {
                        ModernPrimaryButton(text = "Сменить группу", onClick = onEditClick)
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = premiumCardAppearAnimation(3),
                        exit = fadeOut()
                    ) {
                        ModernSecondaryButton(text = "Выйти из аккаунта", onClick = onLogoutClick)
                    }
                }
            } else {
                // Контент, если профиль не настроен
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = premiumCardAppearAnimation(0),
                        exit = fadeOut()
                    ) {
                        ModernInfoCard(
                            title = "Настройка профиля",
                            icon = Icons.Default.Settings,
                            gradientColors = listOf(colors.softBlueStart, colors.softBlueEnd),
                            content = {
                                Text(
                                    text = "Настройте свой профиль для быстрого доступа к расписанию и другим функциям.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = DesignSpacing.XS)
                                )
                            }
                        )
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = premiumCardAppearAnimation(1),
                        exit = fadeOut()
                    ) {
                        ModernPrimaryButton(text = "Выбрать факультет и группу", onClick = onSelectGroupClick)
                    }
                }
            }
            
            // Дополнительный отступ в конце списка
            item {
                Spacer(modifier = Modifier.height(DesignSpacing.M))
            }
            }
        }
    }
}


// --- Вспомогательные Composable-компоненты ---

private fun sharedSlideIn(delay: Int) = fadeIn(animationSpec = tween(600, delayMillis = delay)) + slideInVertically(
    initialOffsetY = { it / 2 },
    animationSpec = spring()
)

// ModernProfileWelcomeCard - стиль как ModernWelcomeCard из HomeScreen
@Composable
private fun ModernProfileWelcomeCard(group: GroupUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = DesignSpacing.S, // Design Tokens: 8dp
                    shape = RectangleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RectangleShape
                )
                .padding(DesignSpacing.XL) // Design Tokens: 24dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp
            ) {
                // Круглый элемент с группой
                val colors = designColors()
                Box(
                    modifier = Modifier
                        .size(DesignIconSizes.Large * 3.75f) // Design Tokens: 120dp
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    colors.primaryGradientStart,
                                    colors.primaryGradientEnd
                                )
                            ),
                            shape = CircleShape
                        )
                        .shadow(
                            elevation = DesignSpacing.XS, // Design Tokens: 4dp
                            shape = CircleShape,
                            spotColor = Color(0xFF3657FF).copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.name.ifBlank { "Группа ${group.code}" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 3,
                        textAlign = TextAlign.Center
                    )
                }
                
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                    thickness = 1.dp
                )
                
                // Информация о группе
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS)
                ) {
                    Text(
                        text = "Группа",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = group.name.ifBlank { group.code },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileNotSetHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = DesignSpacing.S,
                    shape = RectangleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RectangleShape
                )
                .padding(DesignSpacing.XL)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
            ) {
                val colors = designColors()
                Box(
                    modifier = Modifier
                        .size(DesignIconSizes.Large * 3.75f)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .applyShadow(
                            shadowSpec = DesignShadows.Low,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(DesignIconSizes.Large * 2.5f),
                        tint = colors.primaryLight.copy(alpha = 0.4f)
                    )
                }
                Text(
                    text = "Профиль не настроен",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ModernInfoCard - стиль как ModernGradientCard из HomeScreen (но без onClick)
@Composable
private fun ModernInfoCard(
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == com.example.bteu_schedule.ui.theme.DarkColors.BG
    val cardBackground = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.Surface
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.Border
    } else {
        Color(0x0F0D1333)
    }
    val textColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextPrimary
    } else {
        Color(0xFF0D1333)
    }
    val textSecondaryColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextSecondary
    } else {
        Color(0xFF6B7280)
    }
    val iconBackgroundColor = if (isDarkTheme) {
        gradientColors.first().copy(alpha = 0.2f)
    } else {
        gradientColors.first().copy(alpha = 0.1f)
    }
    
    val cornerRadius = DesignRadius.S // 12dp как в ModernGradientCard
    val iconSize = DesignIconSizes.IconButtonSize // 48dp
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = DesignSpacing.XS, // 4dp
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = Color(0x0F0D1333)
                )
                .background(
                    color = cardBackground,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(
                    horizontal = DesignSpacing.CardPaddingHorizontal,
                    vertical = DesignSpacing.CardPaddingVertical
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
                ) {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .background(
                                color = iconBackgroundColor,
                                shape = RoundedCornerShape(DesignRadius.XS)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = gradientColors.first(),
                            modifier = Modifier.size(DesignIconSizes.Medium)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                content()
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    val colors = designColors()
    val isDarkTheme = MaterialTheme.colorScheme.background == com.example.bteu_schedule.ui.theme.DarkColors.BG
    
    // Design Tokens: Светлые карточки с границей и тенью
    val cardBackground = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.Surface
    } else {
        MaterialTheme.colorScheme.surface // --surface-1: #FFFFFF
    }
    val borderColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.Border
    } else {
        Color(0x0F0D1333) // --border: rgba(13,19,51,0.06)
    }
    val textColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextPrimary
    } else {
        Color(0xFF0D1333) // --text-primary: #0D1333
    }
    
    val cardShape = RoundedCornerShape(DesignRadius.M) // Design Tokens: 16dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .applyShadow(
                shadowSpec = DesignShadows.Low, // Design Tokens: тень 4dp
                shape = cardShape
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = cardShape
            ),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Тень через applyShadow
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(
            modifier = Modifier.padding(DesignSpacing.L), // Design Tokens: 20dp
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primaryLight,
                    modifier = Modifier.size(DesignIconSizes.Medium) // Design Tokens: 24dp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall, // Design Tokens: 18sp/24px 600
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
            HorizontalDivider(color = borderColor.copy(alpha = 0.5f)) // Используем borderColor для разделителя
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val isDarkTheme = MaterialTheme.colorScheme.background == com.example.bteu_schedule.ui.theme.DarkColors.BG
    val textSecondaryColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextSecondary
    } else {
        Color(0xFF6B7280) // --text-secondary: #6B7280
    }
    val textColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextPrimary
    } else {
        Color(0xFF0D1333) // --text-primary: #0D1333
    }
    
    if (value.isNotBlank()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                color = textSecondaryColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge, // Design Tokens: 16sp/22px 400
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

// ModernPrimaryButton - современная кнопка в стиле HomeScreen
@Composable
private fun ModernPrimaryButton(text: String, onClick: () -> Unit) {
    val colors = designColors()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "buttonScale"
    )
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(DesignHeights.Button)
            .scale(scale),
        shape = RoundedCornerShape(DesignRadius.M),
        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryLight)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ModernSecondaryButton - современная вторичная кнопка
@Composable
private fun ModernSecondaryButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "buttonScale"
    )
    
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    val colors = designColors()
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(DesignHeights.Button), // Design Tokens: 48dp
        shape = RoundedCornerShape(DesignRadius.M), // Design Tokens: 16dp
        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryLight)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium, // Design Tokens: 16sp/22px Medium
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SecondaryActionButton(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(text = text, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}

private fun getFacultyNameRu(facultyCode: String?): String {
    return when (facultyCode) {
        "FEU" -> "Факультет экономики и управления"
        "FKF" -> "Факультет коммерции и финансов"
        "FKIF" -> "Факультет коммерции и финансов"
        "FPKP" -> "Факультет повышения квалификации и переподготовки"
        else -> facultyCode ?: ""
    }
}

/**
 * Получает название факультета по коду
 * Поддерживает как числовые коды (01, 02), так и текстовые (FEU, FKF)
 */
private fun getFacultyNameByCode(facultyCode: String?): String? {
    if (facultyCode.isNullOrBlank()) return null
    
    // Если код - это число (01, 02 и т.д.), пробуем найти по числовому коду
    if (facultyCode.matches(Regex("^\\d+$"))) {
        return when (facultyCode.trim()) {
            "01" -> "Факультет экономики и управления"
            "02" -> "Факультет коммерции и финансов"
            "03" -> "Факультет повышения квалификации и переподготовки"
            "04" -> "Факультет экономики и управления"
            "05" -> "Факультет коммерции и финансов"
            "06" -> "Факультет повышения квалификации и переподготовки"
            "07" -> "Факультет повышения квалификации и переподготовки"
            else -> null
        }
    }
    
    // Для текстовых кодов используем существующую функцию
    return getFacultyNameRu(facultyCode).takeIf { it != facultyCode }
}

private fun getEducationFormRu(educationForm: String?): String {
    return when (educationForm) {
        "full_time" -> "Очная форма"
        "part_time" -> "Заочная форма"
        "evening" -> "Вечерняя форма"
        else -> educationForm ?: "Не указана"
    }
}
