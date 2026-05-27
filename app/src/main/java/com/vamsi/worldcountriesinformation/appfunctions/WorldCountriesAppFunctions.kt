package com.vamsi.worldcountriesinformation.appfunctions

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.vamsi.worldcountriesinformation.domain.countries.SearchCountriesUseCase
import com.vamsi.worldcountriesinformation.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Assistant-facing app functions for search, open, and compare flows.
 * Delegates to existing domain use cases and deep-link intents.
 */
@Singleton
class WorldCountriesAppFunctions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchCountriesUseCase: SearchCountriesUseCase,
) {

    suspend fun searchCountries(query: String): List<AppFunctionCountryResult> {
        if (query.isBlank()) return emptyList()
        return searchCountriesUseCase(query).first()
            .take(MAX_RESULTS)
            .map { AppFunctionCountryResult(it.name, it.threeLetterCode, it.region) }
    }

    fun openCountry(countryCode: String): Intent {
        val normalized = countryCode.uppercase().trim()
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://worldcountries.vamsi.dev/country/$normalized")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_COUNTRY_CODE, normalized)
        }
    }

    fun compareCountries(codes: List<String>): Intent {
        val normalized = codes.map { it.uppercase().trim() }.filter { it.length == 3 }.take(3)
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("wci://compare/${normalized.joinToString(",")}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    data class AppFunctionCountryResult(
        val name: String,
        val code: String,
        val region: String,
    )

    companion object {
        private const val MAX_RESULTS = 5
    }
}
