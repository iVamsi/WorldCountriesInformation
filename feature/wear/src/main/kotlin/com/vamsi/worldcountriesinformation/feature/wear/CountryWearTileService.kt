package com.vamsi.worldcountriesinformation.feature.wear

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.wear.tiles.GlanceTileService
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetData
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WearTileEntryPoint {
    fun widgetDataSource(): WidgetDataSource
}

class CountryWearTileService : GlanceTileService() {

    @GlanceComposable
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val widgetData by produceState<WidgetData?>(initialValue = null, context) {
            value = loadWidgetData(context)
        }

        GlanceTheme {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    widgetData?.featuredCountry != null -> {
                        val country = widgetData!!.featuredCountry!!
                        Text(
                            text = "Country of Day",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.primary,
                            ),
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = flagEmoji(country.twoLetterCode),
                            style = TextStyle(fontSize = 32.sp),
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = country.name,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.onSurface,
                            ),
                            maxLines = 2,
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = country.capital,
                            style = TextStyle(
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                            maxLines = 1,
                        )
                    }

                    widgetData?.error != null -> {
                        Text(
                            text = "Unable to load",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.error,
                            ),
                        )
                    }

                    widgetData == null -> {
                        Text(
                            text = "Loading…",
                            style = TextStyle(
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                        )
                    }

                    else -> {
                        Text(
                            text = "Open app to load countries",
                            style = TextStyle(
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadWidgetData(context: android.content.Context): WidgetData = try {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WearTileEntryPoint::class.java,
        )
        entryPoint.widgetDataSource().getWidgetData()
    } catch (e: Exception) {
        Timber.e(e, "Failed to load wear tile data")
        WidgetData(
            featuredCountry = null,
            totalCountries = 0,
            isLoading = false,
            error = e.message,
        )
    }

    private fun flagEmoji(countryCode: String): String = try {
        val code = countryCode.uppercase()
        val firstChar = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    } catch (_: Exception) {
        "🏳️"
    }

    companion object {
        fun countryDeepLinkIntent(countryCode: String): Intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://worldcountries.vamsi.dev/country/${countryCode.lowercase()}"),
        ).apply {
            putExtra("extra_country_code", countryCode)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }
}
