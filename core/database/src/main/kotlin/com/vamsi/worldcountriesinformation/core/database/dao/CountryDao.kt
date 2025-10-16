package com.vamsi.worldcountriesinformation.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vamsi.worldcountriesinformation.core.database.entity.CountryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Country entities
 */
@Dao
interface CountryDao {

    /**
     * Get all countries from the database as a Flow for reactive updates
     */
    @Query("SELECT * FROM countries ORDER BY name ASC")
    fun getAllCountries(): Flow<List<CountryEntity>>

    /**
     * Get all countries as a one-time snapshot (non-Flow)
     */
    @Query("SELECT * FROM countries ORDER BY name ASC")
    suspend fun getAllCountriesOnce(): List<CountryEntity>

    /**
     * Get a specific country by its three-letter code
     */
    @Query("SELECT * FROM countries WHERE threeLetterCode = :code")
    fun getCountryByCode(code: String): Flow<CountryEntity?>

    /**
     * Get a specific country by its three-letter code (one-time)
     */
    @Query("SELECT * FROM countries WHERE threeLetterCode = :code")
    suspend fun getCountryByCodeOnce(code: String): CountryEntity?

    /**
     * Search countries by name (case-insensitive)
     */
    @Query("SELECT * FROM countries WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCountries(query: String): Flow<List<CountryEntity>>

    /**
     * Get countries by region
     */
    @Query("SELECT * FROM countries WHERE region = :region ORDER BY name ASC")
    fun getCountriesByRegion(region: String): Flow<List<CountryEntity>>

    /**
     * Insert a single country (replace on conflict)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: CountryEntity)

    /**
     * Insert multiple countries (replace on conflict)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    /**
     * Delete all countries from the database
     */
    @Query("DELETE FROM countries")
    suspend fun deleteAllCountries()

    /**
     * Delete a specific country by code
     */
    @Query("DELETE FROM countries WHERE threeLetterCode = :code")
    suspend fun deleteCountry(code: String)

    /**
     * Get the count of countries in the database
     */
    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCountriesCount(): Int

    /**
     * Check if database is empty
     */
    @Query("SELECT COUNT(*) FROM countries")
    fun getCountriesCountFlow(): Flow<Int>

    /**
     * Get countries older than specified timestamp (for cache invalidation)
     */
    @Query("SELECT * FROM countries WHERE lastUpdated < :timestamp")
    suspend fun getStaleCountries(timestamp: Long): List<CountryEntity>

    /**
     * Refresh all countries (clear and insert new data)
     */
    @Transaction
    suspend fun refreshCountries(countries: List<CountryEntity>) {
        deleteAllCountries()
        insertCountries(countries)
    }
}
