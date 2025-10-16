package com.vamsi.worldcountriesinformation.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vamsi.worldcountriesinformation.data.local.converter.Converters
import com.vamsi.worldcountriesinformation.data.local.dao.CountryDao
import com.vamsi.worldcountriesinformation.data.local.entity.CountryEntity

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
