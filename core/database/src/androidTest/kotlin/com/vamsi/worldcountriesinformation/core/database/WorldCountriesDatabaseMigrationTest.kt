package com.vamsi.worldcountriesinformation.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorldCountriesDatabaseMigrationTest {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WorldCountriesDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_preservesExistingRows_andBackfillsNewColumns() {
        migrationTestHelper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO countries (
                    threeLetterCode,
                    twoLetterCode,
                    name,
                    capital,
                    region,
                    population,
                    callingCode,
                    latitude,
                    longitude,
                    languages,
                    currencies,
                    lastUpdated
                ) VALUES (
                    'IND',
                    'IN',
                    'India',
                    'New Delhi',
                    'Asia',
                    1295210000,
                    '91',
                    20.0,
                    77.0,
                    '[]',
                    '[]',
                    123456789
                )
                """.trimIndent()
            )
            close()
        }

        migrationTestHelper.runMigrationsAndValidate(
            TEST_DB_NAME,
            2,
            true,
            WorldCountriesDatabase.MIGRATION_1_2,
        ).query(
            "SELECT name, subregion, area FROM countries WHERE threeLetterCode = 'IND'"
        ).use { cursor ->
            assertThat(cursor, notNullValue())
            assertThat(cursor.moveToFirst(), equalTo(true))
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("name")), equalTo("India"))
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("subregion")), equalTo(""))
            assertThat(cursor.getDouble(cursor.getColumnIndexOrThrow("area")), equalTo(0.0))
        }
    }

    companion object {
        private const val TEST_DB_NAME = "world-countries-migration-test"
    }
}
