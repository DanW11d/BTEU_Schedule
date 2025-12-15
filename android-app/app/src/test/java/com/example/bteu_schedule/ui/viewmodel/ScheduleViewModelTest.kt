package com.example.bteu_schedule.ui.viewmodel

import app.cash.turbine.test
import com.example.bteu_schedule.data.repository.CachedScheduleRepository
import com.example.bteu_schedule.domain.models.LessonUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import com.google.common.truth.Truth.assertThat

/**
 * Unit тесты для ScheduleViewModel
 * 
 * Тестирует:
 * - Загрузку расписания
 * - Обработку ошибок
 * - Состояния UI (Loading, Success, Error, Empty)
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ScheduleViewModelTest {

    @Mock
    private lateinit var repository: CachedScheduleRepository

    private lateinit var viewModel: ScheduleViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScheduleViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSchedule with valid group code emits Success state with lessons`() = runTest {
        // Arrange
        val groupCode = "12345"
        val dayOfWeek = 1
        val isOddWeek = true
        val mockLessons = listOf(
            LessonUi(
                id = 1,
                pairNumber = 1,
                dayOfWeek = 1,
                time = "09:00-10:35",
                subject = "Математика",
                teacher = "Иванов И.И.",
                classroom = "101",
                type = "lecture",
                weekParity = "odd"
            )
        )

        whenever(repository.getDaySchedule(any(), any(), any()))
            .thenReturn(flowOf(mockLessons))

        // Act
        viewModel.loadSchedule(groupCode, dayOfWeek, isOddWeek)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(ScheduleUiState.Loading::class.java)

            val successState = awaitItem()
            assertThat(successState).isInstanceOf(ScheduleUiState.Success::class.java)
            assertThat((successState as ScheduleUiState.Success).lessons).hasSize(1)
            assertThat(successState.lessons[0].subject).isEqualTo("Математика")
        }
    }

    @Test
    fun `loadSchedule with empty group code emits Empty state`() = runTest {
        // Arrange
        val groupCode = ""
        val dayOfWeek = 1
        val isOddWeek = true

        // Act
        viewModel.loadSchedule(groupCode, dayOfWeek, isOddWeek)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(ScheduleUiState.Loading::class.java)

            val emptyState = awaitItem()
            assertThat(emptyState).isInstanceOf(ScheduleUiState.Empty::class.java)
        }
    }

    @Test
    fun `loadSchedule with empty lessons list emits Empty state`() = runTest {
        // Arrange
        val groupCode = "12345"
        val dayOfWeek = 1
        val isOddWeek = true

        whenever(repository.getDaySchedule(any(), any(), any()))
            .thenReturn(flowOf(emptyList()))

        // Act
        viewModel.loadSchedule(groupCode, dayOfWeek, isOddWeek)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(ScheduleUiState.Loading::class.java)

            val emptyState = awaitItem()
            assertThat(emptyState).isInstanceOf(ScheduleUiState.Empty::class.java)
        }
    }

    @Test
    fun `loadSchedule with repository error emits Error state`() = runTest {
        // Arrange
        val groupCode = "12345"
        val dayOfWeek = 1
        val isOddWeek = true
        val errorMessage = "Network error"

        whenever(repository.getDaySchedule(any(), any(), any()))
            .thenReturn(flow {
                throw RuntimeException(errorMessage)
            })

        // Act
        viewModel.loadSchedule(groupCode, dayOfWeek, isOddWeek)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(ScheduleUiState.Loading::class.java)

            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(ScheduleUiState.Error::class.java)
            assertThat((errorState as ScheduleUiState.Error).message).contains(errorMessage)
        }
    }
}

