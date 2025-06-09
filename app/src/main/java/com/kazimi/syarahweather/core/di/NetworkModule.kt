package com.kazimi.syarahweather.core.di

import com.kazimi.syarahweather.BuildConfig
import com.kazimi.syarahweather.data.net.api.WeatherAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideApiKeyInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            
            // Add API key as query parameter to all requests
            val urlWithApiKey = originalUrl.newBuilder()
                .addQueryParameter("appid", BuildConfig.API_KEY)
                .build()
            
            val requestWithApiKey = originalRequest.newBuilder()
                .url(urlWithApiKey)
                .build()
            
            chain.proceed(requestWithApiKey)
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        apiKeyInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor) // Add API key interceptor first
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWeatherApiService(retrofit: Retrofit): WeatherAPI {
        return retrofit.create(WeatherAPI::class.java)
    }
} 