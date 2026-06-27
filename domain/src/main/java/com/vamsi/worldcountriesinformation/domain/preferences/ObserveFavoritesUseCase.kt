package com.vamsi.worldcountriesinformation.domain.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveFavoritesUseCase
@Inject
constructor(
    private val userPreferencesPort: UserPreferencesPort,
) {
    operator fun invoke(): Flow<Set<String>> = userPreferencesPort.userPreferences.map { it.favoriteCountryCodes }
}
