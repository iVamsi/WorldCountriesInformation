package com.vamsi.worldcountriesinformation.domain.preferences

import com.vamsi.worldcountriesinformation.domain.core.UseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class ToggleFavoriteUseCase
@Inject
constructor(
    private val userPreferencesPort: UserPreferencesPort,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : UseCase<String, Unit>(ioDispatcher) {
    override suspend fun execute(parameters: String) {
        userPreferencesPort.toggleFavorite(parameters)
    }
}
