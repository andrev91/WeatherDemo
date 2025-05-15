package com.example.adventure

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import com.example.adventure.di.WorkerModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [WorkerModule::class]
)
object TestAppModuleForWorkManager {
    @Provides
    @Singleton
    fun provideTestWorkManagerConfiguration(
        hiltWorkerFactory: HiltWorkerFactory
    ): Configuration {
        Log.i("TestAppModuleForWorkManager", "Providing TEST WorkManager Configuration with HiltWorkerFactory and SynchronousExecutor")
        return Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory) // Ensure Hilt workers can be created
            .setExecutor(SynchronousExecutor())   // For synchronous test execution
            .setMinimumLoggingLevel(Log.DEBUG) // Verbose logging for tests
            .build()
    }

    @Provides
    @Singleton
    fun provideTestWorkManager(
        @ApplicationContext context: Context,
        configuration: Configuration // Hilt injects the Configuration from the provider above
    ): WorkManager {
        Log.i("TestAppModuleForWorkManager", "Attempting to initialize and provide WorkManager for tests.")
        try {
            return WorkManager.getInstance(context)
        } catch (illegalException: IllegalStateException) {
            Log.i("TestAppModuleForWorkManager", "WorkManager not yet initialized (or error getting instance), initializing now with Hilt-provided config.")
            WorkManager.initialize(context, configuration)
        }
        val wmInstance = WorkManager.getInstance(context)
        Log.i("TestAppModuleForWorkManager", "Successfully provided WorkManager instance: $wmInstance")
        return wmInstance
    }
}