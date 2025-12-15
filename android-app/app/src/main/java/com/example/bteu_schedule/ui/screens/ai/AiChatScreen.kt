package com.example.bteu_schedule.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.ui.components.AdaptiveHeader
import com.example.bteu_schedule.ui.components.HeaderType
import com.example.bteu_schedule.ui.theme.DesignRadius
import com.example.bteu_schedule.ui.theme.DesignSpacing
import com.example.bteu_schedule.ui.theme.DesignIconSizes
import com.example.bteu_schedule.ui.theme.MotionEasing
import com.example.bteu_schedule.ui.theme.designColors
import com.example.bteu_schedule.ui.utils.ScreenUtils
import com.example.bteu_schedule.ui.utils.premiumMessageEnterAnimation
import com.example.bteu_schedule.ui.utils.premiumInputFocusAnimation
import com.example.bteu_schedule.ui.utils.rememberHapticFeedback
import com.example.bteu_schedule.ui.utils.performMessageSent
import com.example.bteu_schedule.ui.utils.performMessageReceived
import com.example.bteu_schedule.ui.viewmodel.AiChatUiState
import com.example.bteu_schedule.ui.viewmodel.AiChatViewModel
import com.example.bteu_schedule.ui.viewmodel.ChatMessage
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    group: GroupUi? = null,
    showBackButton: Boolean = true,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    initialMessage: String? = null,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isAiConfigured by viewModel.isAiConfigured.collectAsState()
    val hapticFeedback = rememberHapticFeedback()
    
    var messageText by remember { mutableStateOf(initialMessage ?: "") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(initialMessage) {
        initialMessage?.let { message ->
            if (message.isNotBlank()) {
                kotlinx.coroutines.delay(300)
                viewModel.sendMessage(message, group?.code)
                messageText = ""
            }
        }
    }
    
    var previousMessagesSize by remember { mutableStateOf(messages.size) }
    LaunchedEffect(messages.size) {
        if (messages.size > previousMessagesSize) {
            val lastMessage = messages.lastOrNull()
            if (lastMessage != null && !lastMessage.isUser) {
                hapticFeedback.performMessageReceived()
            }
        }
        previousMessagesSize = messages.size
    }
    
    LaunchedEffect(Unit) {
        viewModel.checkAiStatus()
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                kotlinx.coroutines.delay(100)
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isScreenVisible = true
    }
    
    val colors = designColors()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AdaptiveHeader(
                title = "AI Помощник",
                subtitle = null,
                isVisible = true,
                isScrolled = false,
                backButton = showBackButton,
                onBackClick = if (showBackButton) onBack else null,
                avatarIcon = if (!showBackButton) Icons.Default.Person else null,
                onAvatarClick = if (!showBackButton) onProfileClick else null,
                headerType = HeaderType.SECONDARY
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(
                    visible = isAiConfigured == false,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    ModernWarningCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = DesignSpacing.Base,
                                vertical = DesignSpacing.M
                            )
                    )
                }
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = DesignSpacing.Base,
                        end = DesignSpacing.Base,
                        top = DesignSpacing.Base,
                        bottom = DesignSpacing.S
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.Base),
                    state = listState
                ) {
                    itemsIndexed(messages) { index, message ->
                        AnimatedMessageItem(
                            message = message,
                            index = index,
                            isVisible = isScreenVisible
                        )
                    }
                    
                    if (uiState is AiChatUiState.Loading) {
                        item {
                            AnimatedLoadingIndicator()
                        }
                    }
                }
            }
            
            val imePadding = WindowInsets.ime.asPaddingValues()
            val imeBottom = imePadding.calculateBottomPadding()
            val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
            val bottomNavigationHeight = 90.dp
            
            ModernMessageInputField(
                message = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        hapticFeedback.performMessageSent()
                        viewModel.sendMessage(messageText, group?.code)
                        messageText = ""
                    }
                },
                enabled = uiState !is AiChatUiState.Loading && isAiConfigured != false,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (imeBottom > 0.dp) {
                            Modifier.imePadding()
                        } else {
                            Modifier.padding(bottom = bottomNavigationHeight + navigationBarsPadding.calculateBottomPadding())
                        }
                    )
            )
        }
    }
}

@Composable
private fun AnimatedAiAvatar(isLoading: Boolean) {
    val colors = designColors()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isLoading) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(ScreenUtils.getAdaptiveCardSize(48.dp))
            .scale(scale)
            .shadow(
                elevation = DesignSpacing.XS,
                shape = RoundedCornerShape(DesignRadius.S)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.primaryGradientStart,
                        colors.primaryGradientEnd
                    )
                ),
                shape = RoundedCornerShape(DesignRadius.S)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ScreenUtils.getAdaptiveIconSize(20.dp)),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(ScreenUtils.getAdaptiveIconSize(24.dp))
            )
        }
    }
}

@Composable
private fun ModernWarningCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(
                elevation = DesignSpacing.S,
                shape = RoundedCornerShape(DesignRadius.M),
                spotColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(DesignRadius.M),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(DesignRadius.M)
                )
                .padding(DesignSpacing.Base)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M)
            ) {
                Box(
                    modifier = Modifier
                        .size(ScreenUtils.getAdaptiveCardSize(48.dp))
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(DesignRadius.S)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(ScreenUtils.getAdaptiveIconSize(24.dp))
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignSpacing.XS)
                ) {
                    Text(
                        text = "ChatGPT API не настроен",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Перейдите в настройки, чтобы ввести API ключ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedMessageItem(
    message: ChatMessage,
    index: Int,
    isVisible: Boolean
) {
    val colors = designColors()
    val isUser = message.isUser
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 180,
                easing = MotionEasing.EaseOutCubic
            )
        ) + slideInVertically(
            initialOffsetY = { DesignSpacing.M.value.toInt() },
            animationSpec = tween(
                durationMillis = 180,
                easing = MotionEasing.EaseOutCubic
            )
        ),
        exit = fadeOut() + slideOutHorizontally(
            targetOffsetX = { if (isUser) it / 2 else -it / 2 }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignSpacing.XS),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(ScreenUtils.getAdaptiveCardSize(40.dp))
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(DesignRadius.M)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.primaryGradientStart,
                                colors.primaryGradientEnd
                            )
                        ),
                        shape = RoundedCornerShape(DesignRadius.M)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(ScreenUtils.getAdaptiveIconSize(20.dp))
                )
            }
            Spacer(modifier = Modifier.width(DesignSpacing.S))
        }
        
        ModernMessageBubble(
            message = message,
            isUser = isUser
        )
        
        if (isUser) {
            Spacer(modifier = Modifier.width(DesignSpacing.S))
            Box(
                modifier = Modifier
                    .size(ScreenUtils.getAdaptiveCardSize(40.dp))
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(DesignRadius.M)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(DesignRadius.M)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(ScreenUtils.getAdaptiveIconSize(20.dp))
                )
            }
        }
        }
    }
}

@Composable
private fun ModernMessageBubble(
    message: ChatMessage,
    isUser: Boolean
) {
    val colors = designColors()
    val isError = !isUser && (
        message.text.contains("Ошибка:", ignoreCase = true) ||
        message.text.contains("error", ignoreCase = true) ||
        message.text.startsWith("{") && message.text.contains("\"error\"")
    )
    
    val displayText = if (isError) {
        formatErrorMessage(message.text)
    } else {
        message.text
    }
    
    val cornerRadius = DesignRadius.M
    val smallCornerRadius = DesignRadius.XS
    
    Card(
        modifier = Modifier
            .widthIn(max = ScreenUtils.getAdaptiveCardSize(280.dp))
            .shadow(
                elevation = if (isUser) DesignSpacing.XS else 3.dp,
                shape = RoundedCornerShape(
                    topStart = if (isUser) cornerRadius else smallCornerRadius,
                    topEnd = if (isUser) smallCornerRadius else cornerRadius,
                    bottomStart = cornerRadius,
                    bottomEnd = cornerRadius
                ),
                spotColor = if (isError) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                } else if (isUser) {
                    colors.primaryLight.copy(alpha = 0.2f)
                } else {
                    Color.Transparent
                }
            ),
        shape = RoundedCornerShape(
            topStart = if (isUser) cornerRadius else smallCornerRadius,
            topEnd = if (isUser) smallCornerRadius else cornerRadius,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(
                                colors.primaryGradientStart,
                                colors.primaryGradientEnd
                            )
                        )
                    } else if (isError) {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(
                        topStart = if (isUser) cornerRadius else smallCornerRadius,
                        topEnd = if (isUser) smallCornerRadius else cornerRadius,
                        bottomStart = cornerRadius,
                        bottomEnd = cornerRadius
                    )
                )
                .padding(
                    horizontal = DesignSpacing.Base,
                    vertical = DesignSpacing.M
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isError) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(ScreenUtils.getAdaptiveIconSize(18.dp))
                        )
                        Text(
                            text = "Произошла ошибка",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isUser -> Color.White
                        isError -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

@Composable
private fun AnimatedLoadingIndicator() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
        Spacer(modifier = Modifier.width(ScreenUtils.getAdaptiveCardSize(50.dp)))
        
        val loadingCornerRadius = DesignRadius.M
        Card(
            modifier = Modifier
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(loadingCornerRadius),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(loadingCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                            )
                        ),
                        shape = RoundedCornerShape(loadingCornerRadius)
                    )
                    .padding(DesignSpacing.Base)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignSpacing.M)
                ) {
                    AnimatedDots()
                    Text(
                        text = "Думаю...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 0, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    val colors = designColors()
    val dotSize = DesignSpacing.S
    val dotColor = colors.primaryLight
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(DesignSpacing.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(
                    color = dotColor.copy(alpha = dot1Alpha),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(
                    color = dotColor.copy(alpha = dot2Alpha),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(
                    color = dotColor.copy(alpha = dot3Alpha),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

@Composable
private fun ModernMessageInputField(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = designColors()
    val isButtonEnabled = enabled && message.isNotBlank()
    var isFocused by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    
    val focusState = premiumInputFocusAnimation(isFocused)
    
    val inputFieldScale by animateFloatAsState(
        targetValue = if (isSending) 0.97f else focusState.scale,
        animationSpec = tween(
            durationMillis = 120,
            easing = MotionEasing.EaseOutCubic
        ),
        label = "inputFieldScale"
    )
    
    val sendIconRotation by animateFloatAsState(
        targetValue = if (isSending) 15f else 0f,
        animationSpec = tween(
            durationMillis = 120,
            easing = MotionEasing.EaseOutCubic
        ),
        label = "sendIconRotation"
    )
    
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonEnabled) 1f else 0.9f,
        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),
        label = "buttonScale"
    )
    
    val scope = rememberCoroutineScope()
    val handleSendClick = {
        if (message.isNotBlank()) {
            isSending = true
            onSendClick()
            scope.launch {
                kotlinx.coroutines.delay(150)
                isSending = false
            }
        }
    }
    
    val isDarkTheme = isSystemInDarkTheme()
    val inputBackgroundColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = DesignSpacing.Base,
                    end = DesignSpacing.Base,
                    top = DesignSpacing.S,
                    bottom = DesignSpacing.XS
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignSpacing.S)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .scale(inputFieldScale)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(DesignRadius.L),
                        spotColor = Color.Black.copy(alpha = 0.05f)
                    )
                    .background(
                        color = inputBackgroundColor,
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = DesignSpacing.M,
                            vertical = DesignSpacing.XS
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicTextField(
                        value = message,
                        onValueChange = onMessageChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .onFocusChanged { isFocused = it.isFocused },
                        enabled = enabled,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDarkTheme) {
                                Color.White.copy(alpha = 0.6f)
                            } else {
                                Color.Black.copy(alpha = 0.6f)
                            }
                        ),
                        cursorBrush = SolidColor(colors.primaryLight),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = DesignSpacing.XS)
                            ) {
                                if (message.isEmpty()) {
                                    Text(
                                        text = "Введите вопрос...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDarkTheme) {
                                            Color.White.copy(alpha = 0.6f)
                                        } else {
                                            Color.Black.copy(alpha = 0.6f)
                                        },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(DesignIconSizes.IconButtonSize)
                            .scale(buttonScale)
                            .shadow(
                                elevation = if (isButtonEnabled) DesignSpacing.S else 0.dp,
                                shape = CircleShape,
                                spotColor = if (isButtonEnabled) {
                                    colors.primaryLight.copy(alpha = 0.4f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .background(
                                brush = if (isButtonEnabled) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            colors.primaryGradientStart,
                                            colors.primaryGradientEnd
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                        )
                                    )
                                },
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable(
                                enabled = isButtonEnabled,
                                onClick = handleSendClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = if (isButtonEnabled) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            },
                            modifier = Modifier
                                .size(DesignIconSizes.Medium)
                                .rotate(sendIconRotation)
                        )
                    }
                }
            }
        }
    }
}

private fun formatErrorMessage(errorText: String): String {
    return when {
        errorText.contains("insufficient_quota", ignoreCase = true) -> {
            "Превышен лимит использования API.\n\n" +
            "Проверьте баланс на platform.openai.com/account/billing или пополните счёт."
        }
        errorText.contains("unsupported_country", ignoreCase = true) -> {
            "OpenAI API недоступен в вашем регионе.\n\n" +
            "Попробуйте использовать VPN или обратитесь в поддержку OpenAI."
        }
        errorText.contains("429", ignoreCase = true) -> {
            "Слишком много запросов.\n\n" +
            "Подождите немного и попробуйте снова."
        }
        errorText.contains("401", ignoreCase = true) -> {
            "Неверный API ключ.\n\n" +
            "Проверьте ключ в настройках приложения."
        }
        errorText.contains("Ошибка ChatGPT API", ignoreCase = true) -> {
            val parts = errorText.split("Ошибка ChatGPT API")
            if (parts.size > 1) {
                val errorPart = parts[1]
                when {
                    errorPart.contains("insufficient_quota") -> {
                        "Превышен лимит использования API.\n\n" +
                        "Проверьте баланс на platform.openai.com/account/billing"
                    }
                    errorPart.contains("unsupported_country") -> {
                        "OpenAI API недоступен в вашем регионе.\n\n" +
                        "Попробуйте использовать VPN."
                    }
                    else -> "Произошла ошибка при обращении к ChatGPT API.\n\nПопробуйте позже."
                }
            } else {
                "Произошла ошибка при обращении к ChatGPT API.\n\nПопробуйте позже."
            }
        }
        errorText.startsWith("{") && errorText.contains("\"error\"") -> {
            when {
                errorText.contains("insufficient_quota") -> {
                    "Превышен лимит использования API.\n\n" +
                    "Проверьте баланс на platform.openai.com/account/billing"
                }
                errorText.contains("unsupported_country") -> {
                    "OpenAI API недоступен в вашем регионе.\n\n" +
                    "Попробуйте использовать VPN."
                }
                else -> "Произошла ошибка при обращении к API.\n\nПопробуйте позже."
            }
        }
        else -> errorText
    }
}
