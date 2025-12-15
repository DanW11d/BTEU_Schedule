package com.example.bteu_schedule.ui.screens.onboarding

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.viewmodel.GroupSelectionViewModel
import com.example.bteu_schedule.ui.viewmodel.GroupUiState

/**
 * Карточка группы в стиле приложения - светлая карточка с границей и тенью
 */
@Composable
fun GroupCard(
    group: GroupUi,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
    
    Card(
        modifier = modifier
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
                            imageVector = Icons.Default.People,
                            contentDescription = getDisplayGroupName(group),
                            tint = colors.primaryLight,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS)
                    ) {
                        Text(
                            text = getDisplayGroupName(group),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${group.course} курс",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (group.specialization.isNotBlank()) {
                            Text(
                                text = group.specialization,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelectionScreen(
    facultyCode: String?,
    educationFormCode: String?,
    course: Int?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onGroupSelected: (GroupUi) -> Unit,
    viewModel: GroupSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var hasTriedSync by remember { mutableStateOf(false) }
    val colors = designColors()
    
    // Состояние скролла для анимации
    val scrollState = rememberLazyListState()
    val isScrolled = remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 50
        }
    }
    
    LaunchedEffect(facultyCode, educationFormCode, course) {
        Log.d("GroupSelectionScreen", "═══════════════════════════════════════")
        Log.d("GroupSelectionScreen", "ЗАПУСК ЗАГРУЗКИ ГРУПП")
        Log.d("GroupSelectionScreen", "facultyCode: '$facultyCode'")
        Log.d("GroupSelectionScreen", "educationFormCode: '$educationFormCode'")
        Log.d("GroupSelectionScreen", "course: $course")
        Log.d("GroupSelectionScreen", "═══════════════════════════════════════")
        
        // Загружаем группы только если все параметры валидны
        if (!facultyCode.isNullOrBlank() && !educationFormCode.isNullOrBlank() && course != null && course > 0) {
            viewModel.loadGroups(facultyCode, educationFormCode, course)
        } else {
            Log.w("GroupSelectionScreen", "Параметры невалидны, группы не будут загружены")
        }
    }
    
    // Автоматическая синхронизация при первом открытии, если группы не найдены
    LaunchedEffect(uiState) {
        if (uiState is GroupUiState.Empty && !hasTriedSync) {
            hasTriedSync = true
            Log.d("GroupSelectionScreen", "Группы не найдены, запускаем автоматическую синхронизацию...")
            viewModel.syncData()
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
                title = "Выберите группу",
                subtitle = if (course != null) "$course курс" else null,
                isVisible = true,
                isScrolled = isScrolled.value,
                backButton = true,
                onBackClick = onBack,
                headerType = HeaderType.NAVIGATION
            )
            
            // Контент (группы, загрузка, ошибки и т.д.)
            when (val state = uiState) {
                is GroupUiState.Loading -> {
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
                is GroupUiState.Syncing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignSpacing.Base),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
                        ) {
                            CircularProgressIndicator(
                                color = colors.primaryLight
                            )
                            Text(
                                text = "Синхронизация данных с FTP сервером...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is GroupUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignSpacing.Base),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                        ) {
                            Text(
                                text = "Группы не найдены для выбранного факультета и формы обучения",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = "Попробуйте синхронизировать данные с FTP сервером",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { 
                                    hasTriedSync = false
                                    viewModel.syncData()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(DesignHeights.Button),
                                shape = RoundedCornerShape(DesignRadius.M),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryLight
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(DesignSpacing.XS))
                                Text(
                                    "Синхронизировать данные",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                is GroupUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignSpacing.Base),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { 
                                    viewModel.loadGroups(facultyCode, educationFormCode, course)
                                },
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
                }
                is GroupUiState.Success -> {
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
                        itemsIndexed(state.groups) { index, group ->
                            GroupCard(
                                group = group,
                                index = index,
                                onClick = { onGroupSelected(group) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Получить отображаемое название группы
 * Извлекает правильное читаемое название из name или code
 * Формат: S-41, Б-41, А-31, А-11, П-11, 1-25, 2-01 и т.д.
 */
private fun getDisplayGroupName(group: GroupUi): String {
    // Паттерн для кода группы: буква(ы) + дефис + цифры (например: S-41, Б-41, А-31)
    val groupNamePattern = Regex("""[А-ЯA-ZЁ][А-ЯA-ZЁа-яa-zё]*-?\d+""")
    // Паттерн для формата "X-XX" или "X-XXX" (например: 1-25, 2-01, 4-12)
    val numericPattern = Regex("""^\d+-\d+$""")
    
    // Приоритет: показываем полное имя из group.name, если оно читаемое
    val name = group.name.trim()
    if (name.isNotBlank()) {
        // Проверяем, является ли name просто кодом группы
        val isJustCode = name.matches(groupNamePattern) || 
                        name.matches(numericPattern) ||
                        (name.length <= 10 && (groupNamePattern.containsMatchIn(name) || numericPattern.containsMatchIn(name)))
        
        // Если name не является просто кодом, а содержит читаемый текст, возвращаем его
        if (!isJustCode && name.length > 3) {
            // Проверяем, содержит ли name русские буквы или читаемые слова
            val hasReadableText = name.any { it.isLetter() && (it in 'А'..'я' || it in 'A'..'z' || it == 'Ё' || it == 'ё') } &&
                                  name.count { it.isLetter() } >= 2
            
            if (hasReadableText) {
                // Если name содержит "Группа", возвращаем как есть
                if (name.contains(Regex("""(?:Группа|группа|Group|group)""", RegexOption.IGNORE_CASE))) {
                    return name
                } else {
                    // Если нет слова "Группа", но есть читаемый текст, добавляем "Группа" с кодом
                    val code = group.code.trim()
                    if (code.isNotBlank()) {
                        return "Группа $code $name".trim()
                    } else {
                        return "Группа $name".trim()
                    }
                }
            }
        }
        
        // Если name содержит "Группа" и код, возвращаем как есть
        if (name.contains(Regex("""(?:Группа|группа|Group|group)""", RegexOption.IGNORE_CASE))) {
            return name
        }
    }
    
    // Если name не подходит, формируем полное имя из кода
    val code = group.code.trim()
    if (code.isNotBlank()) {
        // Если code содержит пробелы (например "04  1-25"), извлекаем правильную часть
        if (code.contains(" ")) {
            val parts = code.split(Regex("""\s+"""))
            // Ищем часть с буквой и дефисом (например: "S-41", "Б-41")
            for (part in parts) {
                val match = groupNamePattern.find(part)
                if (match != null) {
                    val found = match.value
                    if (found.length <= 10) {
                        return "Группа $found"
                    }
                }
            }
            // Ищем часть с форматом "X-XX" (например: "1-25", "2-01")
            for (part in parts) {
                if (numericPattern.matches(part) && part.length <= 10) {
                    return "Группа $part"
                }
            }
        } else {
            // Если code соответствует формату группы, формируем полное имя
            if (groupNamePattern.matches(code) || numericPattern.matches(code)) {
                return "Группа $code"
            }
        }
    }
    
    // Если ничего не подошло, пытаемся создать читаемое название на основе specialization
    if (group.specialization.isNotBlank()) {
        val groupNumber = extractGroupNumberFromCode(code)
        if (groupNumber.isNotBlank()) {
            val specLetter = extractFirstLetterFromSpecialization(group.specialization)
            if (specLetter != null) {
                return "Группа $specLetter-$groupNumber"
            }
        }
    }
    
    // В крайнем случае возвращаем "Группа" с кодом или просто "Группа"
    if (code.isNotBlank()) {
        return "Группа $code"
    }
    return "Группа"
}

/**
 * Извлекает номер группы из кода (например, "1-25" -> "25", "04  1-25" -> "25")
 */
private fun extractGroupNumberFromCode(code: String): String {
    if (code.isBlank()) return ""
    
    // Если код содержит пробелы, берем последнюю часть
    if (code.contains(" ")) {
        val parts = code.split(Regex("""\s+"""))
        val lastPart = parts.lastOrNull()?.trim() ?: return ""
        // Если последняя часть содержит дефис, берем часть после дефиса
        if (lastPart.contains("-")) {
            val numberPart = lastPart.split("-").lastOrNull()?.trim() ?: return ""
            return numberPart
        }
        return lastPart
    }
    
    // Если код содержит дефис, берем часть после дефиса
    if (code.contains("-")) {
        val numberPart = code.split("-").lastOrNull()?.trim() ?: return ""
        return numberPart
    }
    
    return code
}

/**
 * Извлекает первую букву из специализации для создания читаемого названия группы
 */
private fun extractFirstLetterFromSpecialization(specialization: String): String? {
    if (specialization.isBlank()) return null
    
    val cleanSpec = specialization.trim()
    
    // Пытаемся найти первую букву (кириллица или латиница)
    val letterMatch = Regex("""[А-ЯA-ZЁ]""").find(cleanSpec)
    if (letterMatch != null) {
        return letterMatch.value
    }
    
    // Если не нашли букву, возвращаем null
    return null
}
