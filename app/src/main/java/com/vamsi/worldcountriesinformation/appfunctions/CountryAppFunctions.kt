package com.vamsi.worldcountriesinformation.appfunctions

import android.content.Context
import android.content.Intent
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.vamsi.worldcountriesinformation.domain.countries.SearchCountriesUseCase
import com.vamsi.worldcountriesinformation.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Platform AppFunctions exposed to system assistants for country lookup flows.
 */
@Singleton
class CountryAppFunctions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchCountriesUseCase: SearchCountriesUseCase,
) {

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class SearchCountriesParams(
        /** Country name, capital, or region fragment to search for. */
        val query: String,
    )

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class CountryMatch(
        val name: String,
        val code: String,
        val region: String,
    )

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class SearchCountriesResponse(
        val countries: List<CountryMatch>,
    )

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class OpenCountryParams(
        /** ISO 3166-1 alpha-3 country code, e.g. USA. */
        val countryCode: String,
    )

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class OpenCountryResponse(
        val opened: Boolean,
        val countryCode: String,
    )

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class CompareCountriesParams(
        /** Two or three ISO alpha-3 codes, comma-separated (e.g. USA,CAN). */
        val countryCodes: String,
    )

    @AppFunctionSerializable(isDescribedByKDoc = true)
    data class CompareCountriesResponse(
        val opened: Boolean,
        val countryCodes: String,
    )

    /**
     * Search cached countries by name, capital, or region.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun searchCountries(
        appFunctionContext: AppFunctionContext,
        params: SearchCountriesParams,
    ): SearchCountriesResponse = withContext(Dispatchers.IO) {
        val query = params.query.trim()
        if (query.isBlank()) {
            return@withContext SearchCountriesResponse(emptyList())
        }
        val matches = searchCountriesUseCase(query).first()
            .take(MAX_RESULTS)
            .map { CountryMatch(it.name, it.threeLetterCode, it.region) }
        SearchCountriesResponse(matches)
    }

    /**
     * Open the country details screen for the given alpha-3 code.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun openCountry(
        appFunctionContext: AppFunctionContext,
        params: OpenCountryParams,
    ): OpenCountryResponse = withContext(Dispatchers.Main) {
        val code = params.countryCode.uppercase().trim()
        if (code.length != 3 || !code.all { it.isLetter() }) {
            return@withContext OpenCountryResponse(opened = false, countryCode = code)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse("https://worldcountries.vamsi.dev/country/$code")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_COUNTRY_CODE, code)
        }
        context.startActivity(intent)
        OpenCountryResponse(opened = true, countryCode = code)
    }

    /**
     * Open the compare screen for two or three countries.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun compareCountries(
        appFunctionContext: AppFunctionContext,
        params: CompareCountriesParams,
    ): CompareCountriesResponse = withContext(Dispatchers.Main) {
        val normalized = params.countryCodes.split(',')
            .map { it.uppercase().trim() }
            .filter { it.length == 3 && it.all { ch -> ch.isLetter() } }
            .take(3)
        if (normalized.size < 2) {
            return@withContext CompareCountriesResponse(
                opened = false,
                countryCodes = params.countryCodes,
            )
        }
        val joined = normalized.joinToString(",")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_COMPARE_CODES, joined)
        }
        context.startActivity(intent)
        CompareCountriesResponse(opened = true, countryCodes = joined)
    }

    companion object {
        private const val MAX_RESULTS = 5
    }
}
