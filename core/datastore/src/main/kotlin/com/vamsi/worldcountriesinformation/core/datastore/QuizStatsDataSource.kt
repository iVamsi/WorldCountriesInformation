package com.vamsi.worldcountriesinformation.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quizStatsStore: DataStore<Preferences> by preferencesDataStore(
    name = "quiz_stats",
)

@Singleton
class QuizStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val HIGH_SCORE = intPreferencesKey("high_score")
        val BEST_STREAK = intPreferencesKey("best_streak")
    }

    val quizStats: Flow<QuizStats> = context.quizStatsStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            QuizStats(
                highScore = prefs[Keys.HIGH_SCORE] ?: 0,
                bestStreak = prefs[Keys.BEST_STREAK] ?: 0,
            )
        }

    suspend fun recordSessionResult(score: Int, streak: Int) {
        context.quizStatsStore.edit { prefs ->
            val currentHigh = prefs[Keys.HIGH_SCORE] ?: 0
            val currentBestStreak = prefs[Keys.BEST_STREAK] ?: 0
            if (score > currentHigh) {
                prefs[Keys.HIGH_SCORE] = score
            }
            if (streak > currentBestStreak) {
                prefs[Keys.BEST_STREAK] = streak
            }
        }
    }
}
