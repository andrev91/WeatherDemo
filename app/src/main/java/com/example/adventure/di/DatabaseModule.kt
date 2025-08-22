package com.example.adventure.di

import android.content.Context
import com.example.adventure.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideLocationDao(appDatabase: AppDatabase) = appDatabase.locationDao()

    @Provides
    fun provideBookmarkDao(appDatabase: AppDatabase) = appDatabase.bookmarkDao()

}