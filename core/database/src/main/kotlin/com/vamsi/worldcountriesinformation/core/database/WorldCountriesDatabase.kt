package com.vamsi.worldcountriesinformation.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vamsi.worldcountriesinformation.core.database.converter.Converters
import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity

/**
 * Room database for World Countries Information app
 */
@Database(
    entities = [CountryEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WorldCountriesDatabase : RoomDatabase() {

    abstract fun countryDao(): CountryDao

    companion object {
        const val DATABASE_NAME = "world_countries_database"
    }
}
