package com.vamsi.worldcountriesinformation.core.database.di

import android.content.Context
import androidx.room.Room
import com.vamsi.worldcountriesinformation.core.database.WorldCountriesDatabase
import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance
     */
    @Provides
    @Singleton
    fun provideWorldCountriesDatabase(
        @ApplicationContext context: Context
    ): WorldCountriesDatabase {
        return Room.databaseBuilder(
            context,
            WorldCountriesDatabase::class.java,
            WorldCountriesDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the CountryDao
     */
    @Provides
    @Singleton
    fun provideCountryDao(database: WorldCountriesDatabase): CountryDao {
        return database.countryDao()
    }
}
