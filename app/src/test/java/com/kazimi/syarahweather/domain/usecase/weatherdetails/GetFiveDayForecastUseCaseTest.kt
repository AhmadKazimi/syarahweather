package com.kazimi.syarahweather.domain.usecase.weatherdetails

import app.cash.turbine.test
import com.kazimi.syarahweather.domain.common.Result
import com.kazimi.syarahweather.domain.model.CurrentWeather
import com.kazimi.syarahweather.domain.model.SavedLocation
import com.kazimi.syarahweather.domain.model.WeatherData
import com.kazimi.syarahweather.domain.model.WeatherForecast
import com.kazimi.syarahweather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetFiveDayForecastUseCaseTest {

    @Mock
    private lateinit var weatherRepository: WeatherRepository

    private lateinit var getFiveDayForecastUseCase: GetFiveDayForecastUseCase

    private val testLatitude = 40.7128
    private val testLongitude = -74.0060

    private val testLocation = SavedLocation(
        id = "1",
        name = "New York",
        latitude = testLatitude,
        longitude = testLongitude,
        country = "US",
        state = "NY",
        isCurrentLocation = false
    )

    private val testCurrentWeather = CurrentWeather(
        temperature = 25.0,
        description = "Clear sky",
        iconCode = "01d",
        humidity = 65,
        windSpeed = 5.5,
        feelsLike = 27.0,
        pressure = 1013.0,
        visibility = 10.0,
        uvIndex = 6.0,
        lastUpdated = System.currentTimeMillis()
    )

    private val testForecastList = listOf(
        WeatherForecast(
            date = "2024-01-01 12:00:00",
            maxTemp = 25.0,
            minTemp = 15.0,
            description = "Clear sky",
            iconCode = "01d",
            humidity = 65,
            windSpeed = 5.5
        ),
        WeatherForecast(
            date = "2024-01-01 15:00:00", // Same day - should be filtered out
            maxTemp = 23.0,
            minTemp = 13.0,
            description = "Few clouds",
            iconCode = "02d",
            humidity = 70,
            windSpeed = 4.0
        ),
        WeatherForecast(
            date = "2024-01-02 12:00:00",
            maxTemp = 22.0,
            minTemp = 12.0,
            description = "Partly cloudy",
            iconCode = "02d",
            humidity = 70,
            windSpeed = 4.0
        ),
        WeatherForecast(
            date = "2024-01-03 12:00:00",
            maxTemp = 20.0,
            minTemp = 10.0,
            description = "Cloudy",
            iconCode = "04d",
            humidity = 75,
            windSpeed = 6.0
        ),
        WeatherForecast(
            date = "2024-01-04 12:00:00",
            maxTemp = 18.0,
            minTemp = 8.0,
            description = "Light rain",
            iconCode = "10d",
            humidity = 80,
            windSpeed = 7.0
        ),
        WeatherForecast(
            date = "2024-01-05 12:00:00",
            maxTemp = 16.0,
            minTemp = 6.0,
            description = "Rain",
            iconCode = "10d",
            humidity = 85,
            windSpeed = 8.0
        ),
        WeatherForecast(
            date = "2024-01-06 12:00:00", // Should be filtered out (6th day)
            maxTemp = 14.0,
            minTemp = 4.0,
            description = "Heavy rain",
            iconCode = "09d",
            humidity = 90,
            windSpeed = 10.0
        )
    )

    private val testWeatherData = WeatherData(
        location = testLocation,
        currentWeather = testCurrentWeather,
        forecast = testForecastList
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        getFiveDayForecastUseCase = GetFiveDayForecastUseCase(weatherRepository)
    }

    @Test
    fun `should_ReturnSuccess_When_ValidDataProvided`() = runTest {
        // Given
        whenever(weatherRepository.get5DaysForecastWeather(testLatitude, testLongitude))
            .thenReturn(flowOf(testWeatherData))

        // When
        getFiveDayForecastUseCase(testLatitude, testLongitude).test {
            // Then
            val result = awaitItem()
            assertTrue(result is Result.Success)
            
            val weatherData = result.data
            assertEquals(testLocation, weatherData.location)
            assertEquals(testCurrentWeather, weatherData.currentWeather)
            
            // Should filter to exactly 5 unique days
            assertEquals(5, weatherData.forecast.size)
            
            // Should keep only first occurrence of each day
            assertEquals("2024-01-01 12:00:00", weatherData.forecast[0].date)
            assertEquals("2024-01-02 12:00:00", weatherData.forecast[1].date)
            assertEquals("2024-01-03 12:00:00", weatherData.forecast[2].date)
            assertEquals("2024-01-04 12:00:00", weatherData.forecast[3].date)
            assertEquals("2024-01-05 12:00:00", weatherData.forecast[4].date)
            
            awaitComplete()
        }
    }

    @Test
    fun `should_ReturnCorrectForecastDetails_When_ValidDataProvided`() = runTest {
        // Given
        whenever(weatherRepository.get5DaysForecastWeather(testLatitude, testLongitude))
            .thenReturn(flowOf(testWeatherData))

        // When
        getFiveDayForecastUseCase(testLatitude, testLongitude).test {
            // Then
            val result = awaitItem()
            assertTrue(result is Result.Success)
            
            val forecast = result.data.forecast
            
            // Verify first forecast item details
            val firstForecast = forecast[0]
            assertEquals(25.0, firstForecast.maxTemp)
            assertEquals(15.0, firstForecast.minTemp)
            assertEquals("Clear sky", firstForecast.description)
            assertEquals("01d", firstForecast.iconCode)
            assertEquals(65, firstForecast.humidity)
            assertEquals(5.5, firstForecast.windSpeed)
            
            awaitComplete()
        }
    }

    @Test
    fun `should_HandleLessThanFiveDays_When_InsufficientDataProvided`() = runTest {
        // Given
        val limitedForecastData = testWeatherData.copy(
            forecast = testForecastList.take(3) // Only 3 days
        )
        
        whenever(weatherRepository.get5DaysForecastWeather(testLatitude, testLongitude))
            .thenReturn(flowOf(limitedForecastData))

        // When
        getFiveDayForecastUseCase(testLatitude, testLongitude).test {
            // Then
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(3, result.data.forecast.size)
            
            awaitComplete()
        }
    }

    @Test
    fun `should_ReturnError_When_RepositoryThrowsException`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(weatherRepository.get5DaysForecastWeather(testLatitude, testLongitude))
            .thenThrow(RuntimeException(errorMessage))

        // When
        getFiveDayForecastUseCase(testLatitude, testLongitude).test {
            // Then
            val result = awaitItem()
            assertTrue(result is Result.Error)
            
            awaitComplete()
        }
    }
} 