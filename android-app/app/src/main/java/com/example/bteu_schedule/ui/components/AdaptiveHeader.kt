package com.example.bteu_schedule.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.utils.HeaderAnimationState
import com.example.bteu_schedule.ui.utils.performLightImpact
import com.example.bteu_schedule.ui.utils.premiumHeaderScrollAnimation
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback

enum class HeaderType {
    PRIMARY,
    SECONDARY,
    ASSISTANT,
    NAVIGATION // Restored value
}

data class HeaderAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val badge: Boolean = false
)

@Composable
fun AdaptiveHeader(
    title: String,
    subtitle: String? = null,
    avatarIcon: ImageVector? = null,
    onAvatarClick: (() -> Unit)? = null,
    backButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    actions: List<HeaderAction> = emptyList(),
    isVisible: Boolean = true,
    isScrolled: Boolean = false,
    enableCompressionOnScroll: Boolean = true,
    enableTransparencyOnScroll: Boolean = false, 
    headerType: HeaderType = HeaderType.SECONDARY,
    modifier: Modifier = Modifier
) {
    val systemBarsPadding = WindowInsets.systemBars

    val headerAnimation: HeaderAnimationState = premiumHeaderScrollAnimation(isScrolled = isScrolled)

    val headerHeight by animateDpAsState(
        targetValue = if (enableCompressionOnScroll) headerAnimation.height else 120.dp,
        animationSpec = tween(durationMillis = 300),
        label = "headerHeight"
    )

    val titleFontSize by animateFloatAsState(
        targetValue = if (isScrolled && enableCompressionOnScroll) 22f else 28f,
        animationSpec = tween(durationMillis = 300),
        label = "titleFontSize"
    )

    // USER's CHANGE: Solid primary color background
    val headerBackgroundColor = MaterialTheme.colorScheme.primary

    val headerTextColor = Color.White
    val headerSecondaryTextColor = Color.White.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(headerBackgroundColor)
            .windowInsetsPadding(systemBarsPadding)
    ) {
        if (isVisible) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DesignSpacing.Base),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (backButton && onBackClick != null) {
                    HeaderIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        onClick = onBackClick
                    )
                    Spacer(modifier = Modifier.width(DesignSpacing.M))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = titleFontSize.sp,
                        ),
                        color = headerTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = headerSecondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions.forEach { action ->
                        HeaderIconButton(
                            icon = action.icon,
                            contentDescription = action.contentDescription,
                            onClick = action.onClick,
                            badge = action.badge
                        )
                    }
                }

                if (avatarIcon != null && onAvatarClick != null) {
                    Spacer(modifier = Modifier.width(DesignSpacing.S))
                    HeaderIconButton(
                        icon = avatarIcon,
                        contentDescription = "Профиль",
                        onClick = onAvatarClick
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveHeader(
    title: String,
    subtitle: String? = null,
    isVisible: Boolean = true,
    isScrolled: Boolean = false,
    currentGroup: GroupUi? = null,
    onProfileClick: (() -> Unit)? = null,
    enableTransparencyOnScroll: Boolean = false,
    headerType: HeaderType = HeaderType.SECONDARY,
    modifier: Modifier = Modifier
) {
    AdaptiveHeader(
        title = title,
        subtitle = subtitle,
        avatarIcon = if (onProfileClick != null) Icons.Filled.Person else null,
        onAvatarClick = onProfileClick,
        backButton = false,
        onBackClick = null,
        actions = emptyList(),
        isVisible = isVisible,
        isScrolled = isScrolled,
        enableCompressionOnScroll = true,
        enableTransparencyOnScroll = enableTransparencyOnScroll,
        headerType = headerType,
        modifier = modifier
    )
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    badge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = rememberHapticFeedback()

    val iconColor = Color.White

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hapticFeedback.performLightImpact()
        }
    }

    val iconTint by animateColorAsState(
        targetValue = if (isPressed) iconColor.copy(alpha = 0.8f) else iconColor,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "iconTint"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(DesignIconSizes.Medium)
        )

        if (badge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(DesignSpacing.M)
                    .background(color = MaterialTheme.colorScheme.error, shape = CircleShape)
            )
        }
    }
}