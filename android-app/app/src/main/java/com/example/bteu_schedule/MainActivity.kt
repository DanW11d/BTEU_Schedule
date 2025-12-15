package com.example.bteu_schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.key
import com.example.bteu_schedule.ui.utils.premiumScreenEnterAnimation
import com.example.bteu_schedule.ui.utils.premiumScreenExitAnimation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.bteu_schedule.data.local.AppStateManager
import com.example.bteu_schedule.domain.models.GroupUi
import com.example.bteu_schedule.domain.models.OnboardingStep
import com.example.bteu_schedule.ui.components.CustomBottomNavigation
import com.example.bteu_schedule.ui.components.NavigationItem
import com.example.bteu_schedule.ui.navigation.AppDestinations
import com.example.bteu_schedule.ui.screens.HomeScreen
import com.example.bteu_schedule.ui.screens.NotificationsScreen
import com.example.bteu_schedule.ui.screens.SearchScreen
import com.example.bteu_schedule.ui.screens.SettingsScreen
import com.example.bteu_schedule.ui.screens.ai.AiChatScreen
import com.example.bteu_schedule.ui.screens.analytics.AnalyticsScreen
import com.example.bteu_schedule.ui.screens.bellschedule.BellScheduleScreen
import com.example.bteu_schedule.ui.screens.departments.DepartmentsScreen
import com.example.bteu_schedule.ui.screens.exams.ExamsScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.bteu_schedule.ui.screens.onboarding.OnboardingNavigation
import com.example.bteu_schedule.ui.screens.OpenAiSettingsScreen
import com.example.bteu_schedule.ui.screens.schedule.ScheduleScreen
import com.example.bteu_schedule.ui.screens.tests.TestsScreen
import com.example.bteu_schedule.ui.theme.BTEU_ScheduleTheme
import com.example.bteu_schedule.ui.viewmodel.ScheduleViewModel
import com.example.bteu_schedule.utils.WeekCalculator
import com.example.bteu_schedule.viewmodel.MainViewModel
import com.example.bteu_schedule.widget.WidgetActions
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import android.content.Intent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            com.example.bteu_schedule.utils.AppLogger.d("MainActivity", "onCreate начат")
            
            // A7.6: Обработка Intent от виджета
            handleWidgetIntent(intent)
            
            // Включить StrictMode для профилирования производительности (только в debug)
            // Временно отключено для диагностики крашей
            // com.example.bteu_schedule.utils.PerformanceProfiler.enableStrictMode()
            
            enableEdgeToEdge()
            com.example.bteu_schedule.utils.AppLogger.d("MainActivity", "enableEdgeToEdge выполнен")
            
            setContent {
                BTEU_ScheduleTheme(themeManager = mainViewModel.themeManager) {
                    BTEU_ScheduleApp(
                        themeManager = mainViewModel.themeManager,
                        languageManager = mainViewModel.languageManager,
                        widgetIntent = intent
                    )
                }
            }
            
            com.example.bteu_schedule.utils.AppLogger.d("MainActivity", "onCreate завершен успешно")
        } catch (e: Exception) {
            com.example.bteu_schedule.utils.AppLogger.e("MainActivity", "КРИТИЧЕСКАЯ ОШИБКА в onCreate", e)
            throw e
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // A7.6: Обработка Intent от виджета при обновлении Activity
        handleWidgetIntent(intent)
        setIntent(intent)
    }
    
    /**
     * A7.6: Обработка Intent от виджета
     */
    private fun handleWidgetIntent(intent: Intent?) {
        if (intent?.action != WidgetActions.ACTION_WIDGET_CLICK) return
        
        val actionType = intent.getStringExtra(WidgetActions.EXTRA_ACTION_TYPE)
        com.example.bteu_schedule.utils.AppLogger.d("MainActivity", "Widget action: $actionType")
        
        // Intent будет обработан в BTEU_ScheduleApp
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BTEU_ScheduleApp(
    themeManager: com.example.bteu_schedule.ui.theme.ThemeManager,
    languageManager: com.example.bteu_schedule.ui.theme.LanguageManager,
    widgetIntent: Intent? = null
) {
    val context = LocalContext.current
    val stateManager = remember { AppStateManager(context) }
    
    // A7.6: Состояния для обработки действий виджета
    var widgetActionType by remember { mutableStateOf<String?>(null) }
    var widgetMessageText by remember { mutableStateOf<String?>(null) }
    var widgetDayOfWeek by remember { mutableStateOf<Int?>(null) }
    var widgetGroupCode by remember { mutableStateOf<String?>(null) }
    var widgetLessonId by remember { mutableStateOf<Int?>(null) }
    
    // A7.6: Обработка Intent от виджета
    LaunchedEffect(widgetIntent) {
        widgetIntent?.let { intent ->
            if (intent.action == WidgetActions.ACTION_WIDGET_CLICK) {
                widgetActionType = intent.getStringExtra(WidgetActions.EXTRA_ACTION_TYPE)
                widgetMessageText = intent.getStringExtra(WidgetActions.EXTRA_MESSAGE_TEXT)
                widgetDayOfWeek = intent.getIntExtra(WidgetActions.EXTRA_DAY_OF_WEEK, -1).takeIf { it > 0 }
                widgetGroupCode = intent.getStringExtra(WidgetActions.EXTRA_GROUP_CODE)
                widgetLessonId = intent.getIntExtra(WidgetActions.EXTRA_LESSON_ID, -1).takeIf { it > 0 }
            }
        }
    }
    
    // Загружаем состояние асинхронно, чтобы не блокировать главный поток
    var currentGroup by rememberSaveable { mutableStateOf<GroupUi?>(null) }
    var onboardingStep by rememberSaveable { mutableStateOf<OnboardingStep?>(null) }
    var currentDestination by rememberSaveable { mutableStateOf<AppDestinations?>(null) }
    
    // Оптимизированная параллельная загрузка состояния
    LaunchedEffect(Unit) {
        try {
            com.example.bteu_schedule.utils.AppLogger.d("BTEU_ScheduleApp", "Начало загрузки состояния (параллельно)")
            val startTime = System.currentTimeMillis()
            
            // Загружаем все три значения параллельно для ускорения запуска
            coroutineScope {
                val groupDeferred = async(Dispatchers.IO) {
                    stateManager.loadCurrentGroup()
                }
                val stepDeferred = async(Dispatchers.IO) {
                    if (stateManager.hasSavedState()) {
                        stateManager.loadOnboardingStep()
                    } else {
                        OnboardingStep.MAIN
                    }
                }
                val destinationDeferred = async(Dispatchers.IO) {
                    stateManager.loadCurrentDestination()
                }
                
                // Ждем завершения всех операций
                val results = awaitAll(groupDeferred, stepDeferred, destinationDeferred)
                currentGroup = results[0] as? GroupUi
                onboardingStep = results[1] as? OnboardingStep ?: OnboardingStep.MAIN
                currentDestination = results[2] as? AppDestinations
            }
            
            val loadTime = System.currentTimeMillis() - startTime
            com.example.bteu_schedule.utils.AppLogger.d("BTEU_ScheduleApp", "Состояние загружено за ${loadTime}ms: group=${currentGroup?.code}, step=$onboardingStep, dest=$currentDestination")
        } catch (e: Exception) {
            com.example.bteu_schedule.utils.AppLogger.e("BTEU_ScheduleApp", "Ошибка загрузки состояния", e)
            // Используем значения по умолчанию при ошибке
            onboardingStep = OnboardingStep.MAIN
            currentDestination = AppDestinations.HOME
        }
    }
    
    // Показываем загрузку, пока состояние не загружено
    // Оптимизация: используем легкий индикатор загрузки
    if (onboardingStep == null || currentDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
        }
        return
    }
    
    var showNotificationSettings by remember { mutableStateOf(false) }
    var showAboutScreen by remember { mutableStateOf(false) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var navigateToSchedule by remember { mutableStateOf(false) }
    var navigateToExams by remember { mutableStateOf(false) }
    var navigateToTests by remember { mutableStateOf(false) }
    var navigateToBellSchedule by remember { mutableStateOf(false) }
    var navigateToDepartments by remember { mutableStateOf(false) }
    var navigateToAnalytics by remember { mutableStateOf(false) }
    var navigateToOpenAiSettings by remember { mutableStateOf(false) }

    LaunchedEffect(currentGroup) { 
        withContext(Dispatchers.IO) {
            stateManager.saveCurrentGroup(currentGroup)
        }
    }
    LaunchedEffect(onboardingStep) { 
        val step = onboardingStep ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            stateManager.saveOnboardingStep(step)
        }
    }
    LaunchedEffect(currentDestination) { 
        val dest = currentDestination ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            stateManager.saveCurrentDestination(dest)
        }
    }

    // Сохраняем текущие значения для безопасного использования
    val safeOnboardingStep = onboardingStep ?: OnboardingStep.MAIN
    val safeCurrentDestination = currentDestination ?: AppDestinations.HOME

    // ViewModel для предзагрузки расписания (ленивая инициализация - создаем только когда нужно)
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()
    
    // Предзагрузка расписания при изменении группы через FTP (без переключения вкладки)
    // Выполняем в фоне, чтобы не блокировать UI
    LaunchedEffect(currentGroup) {
        currentGroup?.let { group ->
            // Предзагружаем расписание для первого дня текущей недели в фоне
            // Это обеспечит, что когда пользователь откроет вкладку "Расписание занятий",
            // данные уже будут загружены
            kotlinx.coroutines.withContext(Dispatchers.Default) {
                val isOddWeek = WeekCalculator.isCurrentWeekOdd()
                scheduleViewModel.loadSchedule(group.code, 1, isOddWeek)
            }
        }
    }

    // A4.8: Размещение навигации
    // Экран выбора группы: BottomNav скрыт (опционально)
    // Оптимизация: создаем navigationItems один раз и кэшируем (доступны для всех экранов)
    val navigationItems = remember {
        AppDestinations.entries.map { NavigationItem(it.label, it.icon) }
    }
    val selectedIndex = remember(safeCurrentDestination) {
        AppDestinations.entries.indexOf(safeCurrentDestination)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    if (safeOnboardingStep != OnboardingStep.MAIN) {
        // A4.8: OnboardingNavigation скрывает навигацию (нет Scaffold с bottomBar)
        OnboardingNavigation(
            currentStep = safeOnboardingStep,
            onStepChanged = { onboardingStep = it },
            onOnboardingComplete = { group ->
                currentGroup = group
                onboardingStep = OnboardingStep.MAIN
            }
        )
    } else {

        // Обработка кнопки "Назад" для главных экранов - не закрываем приложение
        // BackHandler работает только когда нет открытых дополнительных экранов
        val hasOpenScreens = navigateToSchedule || navigateToExams || navigateToTests || 
                            navigateToBellSchedule || navigateToDepartments || 
                            navigateToAnalytics || navigateToOpenAiSettings ||
                            showNotificationSettings || showProfileScreen || showAboutScreen
        
        BackHandler(enabled = !hasOpenScreens) {
            // На главных экранах кнопка "Назад" не должна закрывать приложение
            // Просто игнорируем нажатие
        }

        // A4.8: Размещение навигации
        // Навигация всегда находится внизу: Screen → Content → BottomNav
        // AI-чат: BottomNav виден
        // Экран расписания: BottomNav виден
        // Экран выбора группы: BottomNav скрыт (опционально)
        val shouldShowBottomNav = remember(safeCurrentDestination, hasOpenScreens) {
            // A4.8: Навигация видна для основных экранов
            when (safeCurrentDestination) {
                AppDestinations.HOME,
                AppDestinations.SEARCH,
                AppDestinations.AI_CHAT, // A4.8: AI-чат: BottomNav виден
                AppDestinations.NOTIFICATIONS,
                AppDestinations.SETTINGS -> true
                else -> false
            } && !hasOpenScreens // A4.8: Скрываем навигацию при открытых дополнительных экранах
        }
        
        Scaffold(
            bottomBar = {
                // A4.10: AnimatedVisibility для предотвращения мигания при смене экранов
                AnimatedVisibility(
                    visible = shouldShowBottomNav,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier
                ) {
                    // A4.10: key для стабильности навигации (предотвращает дергание)
                    key(safeCurrentDestination) {
                        CustomBottomNavigation(
                            items = navigationItems,
                            selectedIndex = selectedIndex,
                            onItemSelected = { index ->
                                currentDestination = AppDestinations.entries[index]
                            }
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent // Прозрачный фон для edge-to-edge
        ) { _ ->
            // Премиальные переходы между экранами
            // A3.10: Учитываем настройки доступности анимаций
            val enterTransition = premiumScreenEnterAnimation()
            val exitTransition = premiumScreenExitAnimation()
            AnimatedContent(
                targetState = safeCurrentDestination,
                transitionSpec = {
                    enterTransition togetherWith exitTransition
                },
                label = "screen_transition"
            ) { destination ->
                when (destination) {
                AppDestinations.HOME -> HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                    currentGroup = currentGroup,
                    onScheduleClick = { navigateToSchedule = true },
                    onExamsClick = { navigateToExams = true },
                    onTestsClick = { navigateToTests = true },
                    onBellScheduleClick = { navigateToBellSchedule = true },
                    onDepartmentsClick = { navigateToDepartments = true },
                    onProfileClick = { showProfileScreen = true }
                )

                AppDestinations.SEARCH -> SearchScreen(
                    modifier = Modifier.fillMaxSize(), 
                    currentGroup = currentGroup,
                    onProfileClick = { showProfileScreen = true }
                )
                AppDestinations.AI_CHAT -> {
                    // A4.10: Ассистент открывается прямо в табе (не поверх экранов)
                    AiChatScreen(
                        modifier = Modifier.fillMaxSize(),
                        group = currentGroup,
                        showBackButton = false,
                        onBack = { currentDestination = AppDestinations.HOME },
                        onProfileClick = { showProfileScreen = true },
                        initialMessage = widgetMessageText // A7.6: Предзаполненное сообщение от виджета
                    )
                }
                AppDestinations.NOTIFICATIONS -> NotificationsScreen(
                    modifier = Modifier.fillMaxSize(),
                    onProfileClick = { showProfileScreen = true }
                )

                AppDestinations.SETTINGS -> SettingsScreen(
                    modifier = Modifier.fillMaxSize(),
                    themeManager = themeManager,
                    languageManager = languageManager,
                    currentGroup = currentGroup,
                    onProfileClick = { showProfileScreen = true },
                    onGroupChangeClick = {
                        onboardingStep = OnboardingStep.FACULTY
                        stateManager.clearAll()
                    },
                    onNotificationSettingsClick = { showNotificationSettings = true },
                    onOpenAiSettingsClick = { navigateToOpenAiSettings = true },
                    onClearCacheClick = {
                        // TODO: Реализовать очистку кэша базы данных
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Очистка кэша будет реализована")
                        }
                    },
                    onAboutClick = { showAboutScreen = true },
                    onLogoutClick = {
                        currentGroup = null
                        onboardingStep = OnboardingStep.WELCOME
                        stateManager.clearAll()
                    }
                )
                }
            }
        }
    }

    // A4.8: Обработка навигации к экранам из HomeScreen с анимациями
    // A4.8: Экран расписания: BottomNav виден
    if (navigateToSchedule) {
        BackHandler(onBack = { navigateToSchedule = false })
        Scaffold(
            bottomBar = {
                // A4.8: Экран расписания: BottomNav виден
                CustomBottomNavigation(
                    items = navigationItems,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        currentDestination = AppDestinations.entries[index]
                        if (AppDestinations.entries[index] != AppDestinations.HOME) {
                            navigateToSchedule = false
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { _ ->
            ScheduleScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f), // Плавное появление
                group = currentGroup,
                showBackButton = true,
                onBack = { navigateToSchedule = false },
                viewModel = scheduleViewModel // Используем предзагруженный ViewModel
            )
        }
        return@BTEU_ScheduleApp
    }

    if (navigateToExams) {
        BackHandler(onBack = { navigateToExams = false })
        ExamsScreen(
            modifier = Modifier.fillMaxSize(),
            group = currentGroup,
            showBackButton = true,
            onBack = { navigateToExams = false }
        )
        return@BTEU_ScheduleApp
    }

    if (navigateToTests) {
        BackHandler(onBack = { navigateToTests = false })
        TestsScreen(
            modifier = Modifier.fillMaxSize(),
            group = currentGroup,
            showBackButton = true,
            onBack = { navigateToTests = false }
        )
        return@BTEU_ScheduleApp
    }

    if (navigateToBellSchedule) {
        BackHandler(onBack = { navigateToBellSchedule = false })
        BellScheduleScreen(
            modifier = Modifier.fillMaxSize(),
            showBackButton = true,
            onBack = { navigateToBellSchedule = false }
        )
        return@BTEU_ScheduleApp
    }

    if (navigateToDepartments) {
        BackHandler(onBack = { navigateToDepartments = false })
        DepartmentsScreen(
            modifier = Modifier.fillMaxSize(),
            showBackButton = true,
            onBack = { navigateToDepartments = false }
        )
        return@BTEU_ScheduleApp
    }

    if (navigateToAnalytics) {
        BackHandler(onBack = { navigateToAnalytics = false })
        AnalyticsScreen(
            modifier = Modifier.fillMaxSize(),
            group = currentGroup,
            showBackButton = true,
            onBack = { navigateToAnalytics = false }
        )
        return@BTEU_ScheduleApp
    }


    if (navigateToOpenAiSettings) {
        BackHandler(onBack = { navigateToOpenAiSettings = false })
        OpenAiSettingsScreen(
            modifier = Modifier.fillMaxSize(),
            context = context,
            onBack = { navigateToOpenAiSettings = false },
            onApiKeySaved = { /* Можно обновить статус AI */ }
        )
        return@BTEU_ScheduleApp
    }

    if (showNotificationSettings) {
        BackHandler(onBack = { showNotificationSettings = false })
        com.example.bteu_schedule.ui.screens.NotificationSettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onBack = { showNotificationSettings = false }
        )
        return@BTEU_ScheduleApp
    }

    if (showProfileScreen) {
        BackHandler(onBack = { showProfileScreen = false })
        com.example.bteu_schedule.ui.screens.ProfileScreen(
            modifier = Modifier.fillMaxSize(),
            currentGroup = currentGroup,
            facultyName = currentGroup?.facultyName,
            departmentName = currentGroup?.departmentName,
            onBack = { showProfileScreen = false },
            onEditClick = {
                showProfileScreen = false
                onboardingStep = OnboardingStep.FACULTY
                stateManager.clearAll()
            },
            onLogoutClick = {
                currentGroup = null
                onboardingStep = OnboardingStep.WELCOME
                stateManager.clearAll()
                showProfileScreen = false
            },
            onSelectGroupClick = {
                showProfileScreen = false
                onboardingStep = OnboardingStep.FACULTY
                stateManager.clearAll()
            }
        )
        return@BTEU_ScheduleApp
    }

    if (showAboutScreen) {
        BackHandler(onBack = { showAboutScreen = false })
        com.example.bteu_schedule.ui.screens.AboutScreen(
            onDismiss = { showAboutScreen = false }
        )
        return@BTEU_ScheduleApp
    }
}

@Preview(showBackground = true)
@Composable
fun BTEU_ScheduleAppPreview() {
    // Для Preview используем MaterialTheme напрямую, так как ThemeManager требует инъекцию через Hilt
    androidx.compose.material3.MaterialTheme {
        androidx.compose.material3.Text(
            text = "BTEU Schedule Preview",
            modifier = Modifier.padding(16.dp)
        )
    }
}
