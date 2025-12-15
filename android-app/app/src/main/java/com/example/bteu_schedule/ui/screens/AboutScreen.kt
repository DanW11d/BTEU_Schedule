package com.example.bteu_schedule.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.designColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AboutScreen(
    onDismiss: () -> Unit = {}
) {
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isScreenVisible = true
    }
    
    val colors = designColors()
    
    Box(
        modifier = Modifier
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
            AdaptiveHeader(
                title = "О приложении",
                subtitle = "Информация о приложении",
                isVisible = isScreenVisible,
                isScrolled = false,
                backButton = true,
                onBackClick = onDismiss,
                headerType = HeaderType.SECONDARY
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = DesignSpacing.Base,
                        vertical = DesignSpacing.XL
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.XL)
            ) {
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                            scaleIn(
                                initialScale = 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    AppIconWithOutline(
                        modifier = Modifier
                            .size(DesignIconSizes.Large * 3.75f)
                            .shadow(
                                DesignSpacing.XS,
                                RoundedCornerShape(DesignRadius.L),
                                spotColor = Color(0xFF3657FF).copy(alpha = 0.1f)
                            )
                    )
                }
                
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                            slideInVertically(
                                initialOffsetY = { 20 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "БТЭУ Расписание",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 400)),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Версия 1.5.2",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignSpacing.S))
            
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 500)) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    ModernInfoCard(
                        title = "Описание",
                        icon = Icons.Default.Info,
                        gradientColors = listOf(
                            colors.primaryLight,
                            colors.primary
                        )
                    ) {
                        Text(
                            text = "Мобильное приложение для просмотра расписания занятий Белорусского торгово-экономического университета потребительской кооперации. Удобный доступ к расписанию, уведомлениям и экспорту в календарь.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 600)) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    ModernInfoCard(
                        title = "Об университете",
                        icon = Icons.Default.School,
                        gradientColors = listOf(
                            colors.primary,
                            colors.primaryGradientEnd
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                        ) {
                            Text(
                                text = "Белорусский торгово-экономический университет потребительской кооперации",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "г. Гомель, 2025",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 700)) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    ModernInfoCard(
                        title = "Информация",
                        icon = Icons.Default.Settings,
                        gradientColors = listOf(
                            colors.primaryLight2,
                            colors.primary
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                        ) {
                            InfoRow("Версия", "1.5.2")
                            InfoRow("Дата обновления", "2025")
                            InfoRow("Платформа", "Android")
                        }
                    }
                }
            
                AnimatedVisibility(
                    visible = isScreenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 800)) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA).copy(alpha = 0.15f),
                                        Color(0xFF764BA2).copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(DesignRadius.M)
                            )
                            .shadow(
                                DesignSpacing.XS,
                                RoundedCornerShape(DesignRadius.M),
                                spotColor = Color(0x0F0D1333)
                            )
                            .padding(DesignSpacing.XL),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(DesignIconSizes.Large * 1.75f)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B9D),
                                                Color(0xFFC44569)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconWithOutline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(DesignRadius.L)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.School, 
            contentDescription = "App Icon",
            modifier = Modifier.size(DesignIconSizes.Large * 2),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ModernInfoCard(
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignRadius.M),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(DesignSpacing.Base)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(DesignSpacing.S))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(DesignSpacing.S))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
