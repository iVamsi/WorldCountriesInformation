package com.vamsi.worldcountriesinformation.feature.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetData
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Hilt entry point for accessing dependencies in Glance Widget
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetDataSource(): WidgetDataSource
}

/**
 * Country Widget implementation using Glance
 * Supports multiple sizes: Small (2x2), Medium (3x3), Large (4x4+)
 */
class CountryWidget : GlanceAppWidget() {

    // Support multiple widget sizes
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(120.dp, 120.dp), // Small
            DpSize(180.dp, 180.dp), // Medium
            DpSize(250.dp, 250.dp)  // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch widget data
        val widgetData = withContext(Dispatchers.IO) {
            getWidgetData(context)
        }

        provideContent {
            GlanceTheme {
                CountryWidgetContent(
                    widgetData = widgetData,
                    context = context
                )
            }
        }
    }

    private suspend fun getWidgetData(context: Context): WidgetData {
        return try {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val dataSource = hiltEntryPoint.widgetDataSource()
            val data = dataSource.getWidgetData()

            Timber.d("Widget data loaded: country=${data.featuredCountry?.name}, total=${data.totalCountries}, error=${data.error}")

            data
        } catch (e: Exception) {
            Timber.e(e, "Failed to load widget data")
            WidgetData(
                featuredCountry = null,
                totalCountries = 0,
                isLoading = false,
                error = "Failed to load data: ${e.message}"
            )
        }
    }
}

/**
 * Main widget content composable with size-aware layouts
 */
@Composable
private fun CountryWidgetContent(
    widgetData: WidgetData,
    context: Context,
) {
    val size = LocalSize.current
    val widgetSize = when {
        size.width < 150.dp -> WidgetSize.SMALL
        size.width < 220.dp -> WidgetSize.MEDIUM
        else -> WidgetSize.LARGE
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            widgetData.isLoading -> {
                LoadingState()
            }

            widgetData.error != null -> {
                ErrorState(error = widgetData.error, context = context)
            }

            widgetData.featuredCountry != null -> {
                SuccessState(
                    widgetData = widgetData,
                    widgetSize = widgetSize,
                    context = context
                )
            }

            else -> {
                EmptyState(context = context)
            }
        }
    }
}

/**
 * Widget size categories
 */
private enum class WidgetSize {
    SMALL,  // < 150dp width: Show only flag and name
    MEDIUM, // 150-220dp: Show flag, name, and basic info
    LARGE   // > 220dp: Show all information
}

/**
 * Loading state composable
 */
@Composable
private fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Loading...",
            style = TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onSurface
            )
        )
    }
}

/**
 * Error state composable
 */
@Composable
private fun ErrorState(error: String, context: Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = actionStartActivity(getLaunchIntent(context)))
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_error),
            contentDescription = "Error",
            modifier = GlanceModifier.size(36.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = error,
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.error,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to open app",
            style = TextStyle(
                fontSize = 10.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * Empty state composable with action to open app
 */
@Composable
private fun EmptyState(context: Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = actionStartActivity(getLaunchIntent(context)))
    ) {
        Text(
            text = "🌍",
            style = TextStyle(fontSize = 48.sp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "No data available",
            style = TextStyle(
                fontSize = 13.sp,
                color = GlanceTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to open app\nand load countries",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * Success state - displays country information based on widget size
 */
@Composable
private fun SuccessState(
    widgetData: WidgetData,
    widgetSize: WidgetSize,
    context: Context,
) {
    val country = widgetData.featuredCountry ?: return

    when (widgetSize) {
        WidgetSize.SMALL -> SmallWidgetLayout(country, context)
        WidgetSize.MEDIUM -> MediumWidgetLayout(country, widgetData, context)
        WidgetSize.LARGE -> LargeWidgetLayout(country, widgetData, context)
    }
}

/**
 * Small widget layout - Shows only flag and country name
 */
@Composable
private fun SmallWidgetLayout(country: com.vamsi.worldcountriesinformation.domainmodel.Country, context: Context) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = actionStartActivity(getLaunchIntent(context))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = getFlagEmoji(country.twoLetterCode),
            style = TextStyle(fontSize = 40.sp)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = country.name,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )
    }
}

/**
 * Medium widget layout - Shows flag, name, and key info
 */
@Composable
private fun MediumWidgetLayout(
    country: com.vamsi.worldcountriesinformation.domainmodel.Country,
    widgetData: WidgetData,
    context: Context,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = actionStartActivity(getLaunchIntent(context))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌍 Country of Day",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.primary
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = getFlagEmoji(country.twoLetterCode),
            style = TextStyle(fontSize = 48.sp)
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = country.name,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = "📍 ${country.capital}",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 1
        )

        Text(
            text = "👥 ${formatPopulation(country.population)}",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurface
            )
        )
    }
}

/**
 * Large widget layout - Shows full information
 */
@Composable
private fun LargeWidgetLayout(
    country: com.vamsi.worldcountriesinformation.domainmodel.Country,
    widgetData: WidgetData,
    context: Context,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick = actionStartActivity(getLaunchIntent(context))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌍 Country of the Day",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier.defaultWeight()
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Country Flag Emoji
        Text(
            text = getFlagEmoji(country.twoLetterCode),
            style = TextStyle(fontSize = 56.sp)
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Country Name
        Text(
            text = country.name,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Country Details
        CountryInfoRow(label = "Capital", value = country.capital)
        CountryInfoRow(label = "Region", value = country.region)
        CountryInfoRow(
            label = "Population",
            value = formatPopulation(country.population)
        )

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Total countries footer
        Text(
            text = "Total Countries: ${widgetData.totalCountries}",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.secondary
            )
        )
    }
}

/**
 * Displays a single info row
 */
@Composable
private fun CountryInfoRow(label: String, value: String) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            modifier = GlanceModifier.width(80.dp)
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurface
            )
        )
    }
}

/**
 * Convert country code to flag emoji
 */
private fun getFlagEmoji(countryCode: String): String {
    return try {
        val code = countryCode.uppercase()
        val firstChar = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    } catch (e: Exception) {
        "🏳️" // Default flag if conversion fails
    }
}

/**
 * Format population number with commas
 */
private fun formatPopulation(population: Int): String {
    return when {
        population >= 1_000_000_000 -> "${population / 1_000_000_000}B+"
        population >= 1_000_000 -> "${population / 1_000_000}M+"
        population >= 1_000 -> "${population / 1_000}K+"
        else -> population.toString()
    }
}

/**
 * Get launch intent for the main app
 */
private fun getLaunchIntent(context: Context): Intent {
    return context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?: Intent().apply {
            // Fallback: try to launch main activity
            setClassName(
                context.packageName,
                "${context.packageName}.ui.MainActivity"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
