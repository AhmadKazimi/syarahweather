package com.kazimi.syarahweather.core.di

import com.kazimi.syarahweather.data.datasource.device.PlatformLocationProviderImpl
import com.kazimi.syarahweather.data.datasource.local.LocationDataSourceImpl
import com.kazimi.syarahweather.data.repository.DeviceLocationRepositoryImpl
import com.kazimi.syarahweather.data.repository.LocationRepositoryImpl
import com.kazimi.syarahweather.data.repository.PlacesRepositoryImpl
import com.kazimi.syarahweather.data.repository.WeatherRepositoryImpl
import com.kazimi.syarahweather.domain.datasource.device.PlatformLocationProvider
import com.kazimi.syarahweather.domain.datasource.local.LocationDataSource
import com.kazimi.syarahweather.domain.repository.DeviceLocationRepository
import com.kazimi.syarahweather.domain.repository.LocationRepository
import com.kazimi.syarahweather.domain.repository.PlacesRepository
import com.kazimi.syarahweather.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindLocationDataSource(
        locationDataSourceImpl: LocationDataSourceImpl
    ): LocationDataSource

    @Binds
    @Singleton
    abstract fun bindPlatformLocationProvider(
        platformLocationProviderImpl: PlatformLocationProviderImpl
    ): PlatformLocationProvider

    @Binds
    @Singleton
    abstract fun bindDeviceLocationRepository(
        deviceLocationRepositoryImpl: DeviceLocationRepositoryImpl
    ): DeviceLocationRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
    
    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        placesRepositoryImpl: PlacesRepositoryImpl
    ): PlacesRepository
} 