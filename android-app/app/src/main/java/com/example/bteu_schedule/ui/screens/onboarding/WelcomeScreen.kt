package com.example.bteu_schedule.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignHeights
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.theme.applyShadow
import com.example.bteu_schedule.ui.theme.DesignShadows

@Composable
private fun AppIconWithOutline(
    modifier: Modifier = Modifier,
    colors: com.example.bteu_schedule.ui.theme.DesignColorScheme = designColors()
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.primaryGradientStart,
                        colors.primaryGradientEnd
                    )
                ),
                shape = RoundedCornerShape(DesignRadius.L) // Design Tokens: 24dp
            )
            .applyShadow(
                shadowSpec = DesignShadows.Mid,
                shape = RoundedCornerShape(DesignRadius.L)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Расписание БТЭУ",
            tint = Color.White,
            modifier = Modifier.size(DesignIconSizes.Large * 2.5f) // 80dp
        )
    }
}

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onStartClicked: () -> Unit = {}
) {
    val colors = designColors()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.primaryGradientStart,
                        colors.primaryGradientMid,
                        colors.primaryGradientEnd
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .padding(DesignSpacing.XL), // Design Tokens: 24dp
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
        ) {
            // Иконка приложения с календарем
            AppIconWithOutline(
                modifier = Modifier.size(140.dp),
                colors = colors
            )

            Spacer(modifier = Modifier.height(DesignSpacing.XXL)) // Design Tokens: 32dp

            Text(
                text = "Расписание БТЭУ",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                lineHeight = 40.sp
            )
            Spacer(modifier = Modifier.height(DesignSpacing.S)) // Design Tokens: 8dp
            Text(
                text = "Ваше расписание всегда под рукой",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(DesignSpacing.XXXL)) // Design Tokens: 40dp

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DesignSpacing.M), // Design Tokens: 12dp
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(DesignSpacing.S) // Design Tokens: 8dp
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "Актуальное расписание занятий",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(DesignSpacing.S) // Design Tokens: 8dp
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "Уведомления об изменениях",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M) // Design Tokens: 12dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(DesignSpacing.S) // Design Tokens: 8dp
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "Лекции и практические занятия",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignIconSizes.IconButtonSize)) // Design Tokens: 48dp

            Button(
                onClick = onStartClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignHeights.Button) // Design Tokens: 56dp
                    .applyShadow(
                        shadowSpec = DesignShadows.Mid,
                        shape = RoundedCornerShape(DesignRadius.M)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = colors.primary
                ),
                shape = RoundedCornerShape(DesignRadius.M) // Design Tokens: 16dp
            ) {
                Text(
                    text = "Начать",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

