package com.vamsi.worldcountriesinformation.core.datastore

/**
 * Persisted quiz statistics (offline, local only).
 */
data class QuizStats(
    val highScore: Int = 0,
    val bestStreak: Int = 0,
)
