package com.example.bteu_schedule.data.repository

import com.example.bteu_schedule.data.remote.api.ScheduleApiService
import com.example.bteu_schedule.data.remote.dto.ApiResponse
import com.example.bteu_schedule.data.remote.dto.FacultyDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import retrofit2.Response
import com.google.common.truth.Truth.assertThat

/**
 * Unit тесты для ScheduleRepository
 * 
 * Тестирует:
 * - Получение списка факультетов
 * - Обработку успешных ответов
 * - Обработку ошибок сети
 * - Обработку HTTP ошибок
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ScheduleRepositoryTest {

    @Mock
    private lateinit var apiService: ScheduleApiService

    private lateinit var repository: ScheduleRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = ScheduleRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getFaculties returns Success when API returns successful response`() = runTest {
        // Arrange
        val mockFaculties = listOf(
            FacultyDto(id = 1, code = "FAC1", name = "Факультет 1", description = "Описание 1"),
            FacultyDto(id = 2, code = "FAC2", name = "Факультет 2", description = "Описание 2")
        )
        val response = Response.success(mockFaculties)

        whenever(apiService.getFaculties())
            .thenReturn(response)

        // Act
        val result = repository.getFaculties()

        // Assert
        assertThat(result).isInstanceOf(ApiResponse.Success::class.java)
        val successResult = result as ApiResponse.Success
        assertThat(successResult.data).hasSize(2)
        assertThat(successResult.data[0].code).isEqualTo("FAC1")
        assertThat(successResult.data[1].code).isEqualTo("FAC2")
    }

    @Test
    fun `getFaculties returns Error when API returns error response`() = runTest {
        // Arrange
        val errorBody = "Not Found".toResponseBody()
        val response = Response.error<List<FacultyDto>>(404, errorBody)

        whenever(apiService.getFaculties())
            .thenReturn(response)

        // Act
        val result = repository.getFaculties()

        // Assert
        assertThat(result).isInstanceOf(ApiResponse.Error::class.java)
        val errorResult = result as ApiResponse.Error
        assertThat(errorResult.message).isNotEmpty()
        assertThat(errorResult.code).isEqualTo(404)
    }

    @Test
    fun `getFaculties returns Error when API throws exception`() = runTest {
        // Arrange
        val exception = Exception("Network error")

        whenever(apiService.getFaculties())
            .thenThrow(exception)

        // Act
        val result = repository.getFaculties()

        // Assert
        assertThat(result).isInstanceOf(ApiResponse.Error::class.java)
        val errorResult = result as ApiResponse.Error
        assertThat(errorResult.message).contains("Network error")
    }

    @Test
    fun `getFaculties returns Error when response body is null`() = runTest {
        // Arrange
        val response = Response.success<List<FacultyDto>>(null)

        whenever(apiService.getFaculties())
            .thenReturn(response)

        // Act
        val result = repository.getFaculties()

        // Assert
        assertThat(result).isInstanceOf(ApiResponse.Error::class.java)
        val errorResult = result as ApiResponse.Error
        assertThat(errorResult.message).isNotEmpty()
    }
}

