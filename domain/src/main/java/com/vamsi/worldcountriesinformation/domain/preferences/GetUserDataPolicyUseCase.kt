package com.vamsi.worldcountriesinformation.domain.preferences

import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes user preferences and emits the effective fetch [CachePolicy].
 */
class GetUserDataPolicyUseCase
@Inject
constructor(
    private val userPreferencesPort: UserPreferencesPort,
) {
    operator fun invoke(): Flow<CachePolicy> = userPreferencesPort.userPreferences.map(::resolveFetchPolicy)
}
