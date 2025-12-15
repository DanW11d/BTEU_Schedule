package com.example.bteu_schedule.ui.screens.departments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.DepartmentUi
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignShadows
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.viewmodel.DepartmentsUiState
import com.example.bteu_schedule.ui.viewmodel.DepartmentsViewModel

@Composable
fun DepartmentsScreen(
    modifier: Modifier = Modifier,
    group: GroupUi? = null,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: DepartmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllDepartments()
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
                title = "Кафедры",
                subtitle = "Все кафедры университета",
                isVisible = true,
                isScrolled = false,
                backButton = showBackButton,
                onBackClick = if (showBackButton) onBack else null,
                headerType = if (showBackButton) HeaderType.NAVIGATION else HeaderType.SECONDARY,
                avatarIcon = if (!showBackButton) Icons.Default.Person else null,
                onAvatarClick = if (!showBackButton) onProfileClick else null
            )
            
            // Контент кафедр
            DepartmentsContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState
            )
        }
    }
}

@Composable
private fun DepartmentsContent(
    modifier: Modifier = Modifier,
    uiState: DepartmentsUiState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is DepartmentsUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is DepartmentsUiState.Success -> {
                if (uiState.departments.isEmpty()) {
                    EmptyDepartmentsCard()
                } else {
                    // Группируем кафедры по факультетам
                    val departmentsByFaculty = uiState.departments.groupBy { it.facultyCode }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(DesignSpacing.Base),
                        verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base)
                    ) {
                        // Отображаем кафедры, сгруппированные по факультетам
                        departmentsByFaculty.forEach { (facultyCode, departments) ->
                            item {
                                FacultySection(
                                    facultyCode = facultyCode,
                                    departments = departments
                                )
                            }
                        }
                    }
                }
            }
            is DepartmentsUiState.Error -> {
                ErrorCard(
                    message = uiState.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is DepartmentsUiState.Empty -> {
                EmptyDepartmentsCard()
            }
        }
    }
}

@Composable
private fun FacultySection(
    facultyCode: String,
    departments: List<DepartmentUi>
) {
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
            modifier = Modifier.padding(DesignSpacing.Base), // Design Tokens: 16dp
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp
        ) {
            // Заголовок факультета
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignIconSizes.Medium * 1.5f) // Design Tokens: 36dp
                        .background(
                            color = colors.primaryLight.copy(alpha = 0.1f), // Легкий акцент для иконки
                            shape = RoundedCornerShape(DesignRadius.XS) // Design Tokens: 8dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = colors.primaryLight,
                        modifier = Modifier.size(DesignIconSizes.Medium) // Design Tokens: 24dp
                    )
                }
                Text(
                    text = getFacultyName(facultyCode),
                    style = MaterialTheme.typography.headlineSmall, // Design Tokens: 18sp/24px 600
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = DesignSpacing.XS), // Design Tokens: 4dp
                color = borderColor.copy(alpha = 0.5f) // Используем borderColor для разделителя
            )
            
            // Список кафедр
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
            ) {
                departments.forEach { department ->
                    DepartmentCard(department = department)
                }
            }
        }
    }
}

@Composable
private fun DepartmentCard(department: DepartmentUi) {
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
    val textSecondaryColor = if (isDarkTheme) {
        com.example.bteu_schedule.ui.theme.DarkColors.TextSecondary
    } else {
        Color(0xFF6B7280) // --text-secondary: #6B7280
    }
    
    val cardShape = RoundedCornerShape(DesignRadius.S) // Design Tokens: 12dp для вложенных карточек
    
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSpacing.Base), // Design Tokens: 16dp
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.Base), // Design Tokens: 16dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка кафедры с градиентным фоном
            Box(
                modifier = Modifier
                    .size(DesignIconSizes.IconButtonSize) // Design Tokens: 48dp
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.primaryGradientStart,  // #3A4DFF
                                colors.primaryGradientEnd     // #000064
                            )
                        ),
                        shape = RoundedCornerShape(DesignRadius.XS) // Design Tokens: 8dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(DesignIconSizes.Medium) // Design Tokens: 24dp
                )
            }
            
            // Информация о кафедре
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS) // Design Tokens: 4dp
            ) {
                Text(
                    text = department.name,
                    style = MaterialTheme.typography.titleMedium, // Design Tokens: 16sp/22px Medium
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (department.description != null && department.description.isNotEmpty()) {
                    Text(
                        text = department.description,
                        style = MaterialTheme.typography.bodyMedium, // Design Tokens: 13sp/18px 400
                        color = textSecondaryColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDepartmentsCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSpacing.Base), // Design Tokens: 16dp боковые поля
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignRadius.M), // Design Tokens: 16dp (--radius-lg)
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Тень через shadow modifier
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSpacing.XXL), // Design Tokens: 32dp внутренний padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base) // Design Tokens: 16dp между элементами
            ) {
                // Иконка кафедр
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(DesignIconSizes.Large * 2), // Design Tokens: 64dp для empty state
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Мягкий нейтральный цвет
                )
                
                Text(
                    text = "КАФЕДРЫ НЕ НАЙДЕНЫ",
                    style = MaterialTheme.typography.headlineMedium, // Design Tokens: --type-h2: 20px/28px 600
                    fontWeight = FontWeight.SemiBold, // 600
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Кафедры не загружены",
                    style = MaterialTheme.typography.bodyMedium, // Design Tokens: --type-small: 13px/18px 400
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    // Danger colors - правильные красные цвета для ошибок
    val dangerBackground = Color(0xFFFFE5E5) // #FFE5E5 - фон
    val dangerText = Color(0xFFFF3B30) // #FF3B30 - текст и иконка
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignSpacing.Base),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignRadius.L), // 24dp для больших карточек
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = dangerBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignSpacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = dangerText,
                    modifier = Modifier.size(DesignIconSizes.Large * 2)
                )
                Text(
                    text = "Ошибка загрузки",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = dangerText
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = dangerText.copy(alpha = 0.8f)
                )
            }
        }
    }
}

fun getFacultyName(facultyCode: String): String {
    return when (facultyCode) {
        "FEU" -> "Факультет экономики и управления"
        "FKF" -> "Факультет коммерции и финансов"
        "FPKP" -> "Факультет повышения квалификации и переподготовки"
        "FKIF" -> "Факультет коммерции и финансов"
        else -> "Факультет $facultyCode"
    }
}

