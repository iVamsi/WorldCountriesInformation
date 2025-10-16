package com.vamsi.worldcountriesinformation.di

import android.content.Context
import androidx.room.Room
import com.vamsi.worldcountriesinformation.data.local.WorldCountriesDatabase
import com.vamsi.worldcountriesinformation.data.local.dao.CountryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

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
            .fallbackToDestructiveMigration() // For development - remove in production
            .build()
    }

    @Provides
    @Singleton
    fun provideCountryDao(database: WorldCountriesDatabase): CountryDao {
        return database.countryDao()
    }
}
