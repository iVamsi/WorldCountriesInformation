package com.vamsi.worldcountriesinformation.startup

import com.vamsi.worldcountriesinformation.core.database.dao.CountryDao
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pre-warms the Room countries cache on cold start so the first list render
 * does not pay the SQLite open + initial query cost on the UI thread's
 * dispatch path.
 *
 * Fire-and-forget: failures here must never crash startup.
 */
@Singleton
class CachePreloader @Inject constructor(
    private val countryDao: CountryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun warm() {
        scope.launch {
            runCatching { countryDao.getAllCountriesOnce() }
                .onSuccess { Timber.d("Pre-warmed Room cache: ${it.size} countries") }
                .onFailure { Timber.w(it, "Cache pre-warm failed") }
        }
    }
}
