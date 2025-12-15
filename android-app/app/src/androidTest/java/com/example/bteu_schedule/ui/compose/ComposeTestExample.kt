package com.example.bteu_schedule.ui.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bteu_schedule.ui.theme.BTEU_ScheduleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Примеры Instrumented тестов для Compose UI компонентов
 * 
 * Демонстрирует базовое тестирование UI компонентов
 */
@RunWith(AndroidJUnit4::class)
class ComposeTestExample {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exampleTest() {
        // Пример теста Compose компонента
        composeTestRule.setContent {
            BTEU_ScheduleTheme(darkTheme = false) {
                // Здесь можно тестировать любой Compose компонент
                // Например: HomeScreen, ScheduleScreen и т.д.
            }
        }

        // Примеры проверок:
        // composeTestRule.onNodeWithText("Главная").assertIsDisplayed()
        // composeTestRule.onNodeWithText("Расписание").performClick()
    }

    @Test
    fun testHomeScreenTitle() {
        composeTestRule.setContent {
            BTEU_ScheduleTheme(darkTheme = false) {
                // Здесь можно добавить тест для заголовка HomeScreen
            }
        }

        // Пример проверки наличия текста
        // composeTestRule.onNodeWithText("Расписание").assertIsDisplayed()
    }
}

