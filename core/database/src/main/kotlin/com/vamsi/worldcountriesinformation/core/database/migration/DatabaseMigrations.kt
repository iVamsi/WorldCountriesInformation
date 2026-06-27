package com.vamsi.worldcountriesinformation.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /**
     * No-op migration establishing a versioned upgrade path from destructive fallback.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Schema unchanged between v1 and v2.
        }
    }
}
