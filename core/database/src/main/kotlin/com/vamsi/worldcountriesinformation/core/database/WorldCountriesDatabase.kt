package com.vamsi.worldcountriesinformation.core.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vamsi.worldcountriesinformation.core.database.converter.Converters
import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity

/**
 * Room database for World Countries Information app
 */
@Database(
    entities = [CountryEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WorldCountriesDatabase : RoomDatabase() {

    abstract fun countryDao(): CountryDao

    companion object {
        const val DATABASE_NAME = "world_countries_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE countries ADD COLUMN subregion TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE countries ADD COLUMN area REAL NOT NULL DEFAULT 0.0"
                )
            }
        }
    }
}
