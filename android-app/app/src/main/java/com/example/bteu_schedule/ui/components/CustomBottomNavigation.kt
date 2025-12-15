package com.example.bteu_schedule.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.bteu_schedule.ui.theme.*
import com.example.bteu_schedule.ui.utils.performLightImpact
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback

@Composable
fun CustomBottomNavigation(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isScrollingDown: Boolean = false
) {
    val shouldShowNav = !isScrollingDown

    val navOffsetY by animateDpAsState(
        targetValue = if (shouldShowNav) 0.dp else DesignHeights.BottomNavigation,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "navScrollOffset"
    )

    val backgroundColor = MaterialTheme.colorScheme.surface
    val isDarkTheme = isSystemInDarkTheme()
    val topLineColor = if (isDarkTheme) Color(0x1AFFFFFF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(DesignHeights.BottomNavigation)
            .offset(y = navOffsetY),
        color = Color.Transparent, // The background is handled by the Box inside
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(topStart = DesignRadius.L, topEnd = DesignRadius.L)
                )
                .then(
                    if (!isDarkTheme) {
                        Modifier.applyShadow(
                            shadowSpec = DesignShadows.Low,
                            shape = RoundedCornerShape(topStart = DesignRadius.L, topEnd = DesignRadius.L)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(topLineColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars) // Apply padding only to the content
                    .padding(horizontal = DesignSpacing.Base),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    key(item.label) {
                        ModernNavigationItem(
                            item = item,
                            isSelected = index == selectedIndex,
                            onClick = { onItemSelected(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernNavigationItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = rememberHapticFeedback()
    val isDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hapticFeedback.performLightImpact()
        }
    }

    val backgroundScale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0f,
        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
        label = "backgroundScale"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0f,
        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
        label = "backgroundAlpha"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 160, easing = MotionEasing.EaseOutCubic),
        label = "iconScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(durationMillis = 160, easing = MotionEasing.EaseOutCubic),
        label = "iconColor"
    )

    val circleSize by animateDpAsState(
        targetValue = if (isSelected) DesignIconSizes.IconButtonSize else 44.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circleSize"
    )

    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .sizeIn(minWidth = DesignIconSizes.IconButtonSize, minHeight = DesignIconSizes.IconButtonSize)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Container for layering background and icon
        Box(
            modifier = Modifier.size(circleSize),
            contentAlignment = Alignment.Center
        ) {
            // Animated Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(backgroundScale)
                    .alpha(backgroundAlpha)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary) // Solid primary color
            )

            // Icon - always visible
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .size(DesignIconSizes.Medium)
                    .scale(iconScale)
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector
)
