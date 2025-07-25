package com.example.adventure.network

import com.example.adventure.BuildConfig
import com.example.adventure.api.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

        private const val API_KEY = "KEY"
        private const val URL = "https://dataservice.accuweather.com/"
        private val json = Json { ignoreUnknownKeys = true }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient  {
                return OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                                level = HttpLoggingInterceptor.Level.BODY
                        })
                        .build()
        }

        @Provides
        @Singleton
        fun provideApiService(retrofit: Retrofit) : ApiService {
                return retrofit.create(ApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
                return Retrofit.Builder()
                        .baseUrl(URL)
                        .client(okHttpClient)
                        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                        .build()
        }

        @Provides
        @Singleton
        @ApiKey // Custom qualifier if needed, simple string injection for now
        fun provideApiKey(): String {
                // Basic check - replace with proper validation/handling
                val apiKey = BuildConfig.ACCUWEATHER_API_KEY
                if (apiKey.isBlank() || apiKey == API_KEY) {
                        throw IllegalArgumentException("AccuWeather API Key is not set in BuildConfig. Please add it to your local.properties or gradle file.")
                }
                println("Using API Key: ...${apiKey.takeLast(4)}") // Avoid logging full key
                return apiKey
        }

        @javax.inject.Qualifier
        @Retention(AnnotationRetention.BINARY)
        annotation class ApiKey

}