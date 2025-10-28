package com.vamsi.worldcountriesinformation.core.datastore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt module for DataStore dependencies.
 *
 * Provides JSON serializer for complex preference types.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides a JSON serializer instance.
     *
     * Configured with:
     * - ignoreUnknownKeys: Handles schema changes gracefully
     * - encodeDefaults: Always encode default values
     * - prettyPrint: Human-readable JSON for debugging
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false // Set to true for debugging
    }
}
