@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.vamsi.worldcountriesinformation.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

/**
 * Settings screen for configuring app preferences.
 *
 * Features:
 * - Cache policy selection (4 options)
 * - Offline mode toggle
 * - Cache statistics display
 * - Clear cache functionality
 * - About section
 *
 * @param onNavigateBack Callback when back button is pressed
 * @param viewModel Settings ViewModel (injected by Hilt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onDailyNotificationChanged: (Boolean) -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userPreferences = state.userPreferences
    val cacheStats = state.cacheStats
    val isLoading = state.isLoading
    val context = androidx.compose.ui.platform.LocalContext.current

    var showClearCacheDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsContract.Effect.ShowError -> {
                    SnapNotify.showError(context.message(effect.error))
                }
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            SnapNotify.showError(context.message(error))
            viewModel.processIntent(SettingsContract.Intent.ClearError)
        }
    }

    Scaffold(
        modifier = Modifier.testTag(UiTestTags.SETTINGS_SCREEN),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_navigate_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Cache Policy Section
            SettingsSection(title = stringResource(R.string.settings_section_cache_policy)) {
                CachePolicySelector(
                    selectedPolicy = userPreferences.cachePolicy,
                    onPolicySelected = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateCachePolicy(it))
                    },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_network)) {
                OfflineModeSwitch(
                    enabled = userPreferences.offlineMode,
                    onToggle = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateOfflineMode(it))
                    },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                ThemeModeSelector(
                    selectedMode = userPreferences.themeMode,
                    onModeSelected = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateThemeMode(it))
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                DynamicColorSwitch(
                    enabled = userPreferences.useDynamicColor,
                    onToggle = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateUseDynamicColor(it))
                    },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_features)) {
                FeatureToggleSwitch(
                    title = stringResource(R.string.settings_ai_summaries),
                    description = stringResource(R.string.settings_ai_summaries_desc),
                    enabled = userPreferences.aiSummaryEnabled,
                    onToggle = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateAiSummaryEnabled(it))
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                FeatureToggleSwitch(
                    title = stringResource(R.string.settings_daily_notification),
                    description = stringResource(R.string.settings_daily_notification_desc),
                    enabled = userPreferences.dailyNotificationEnabled,
                    onToggle = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateDailyNotificationEnabled(it))
                        onDailyNotificationChanged(it)
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                FeatureToggleSwitch(
                    title = stringResource(R.string.settings_map_borders),
                    description = stringResource(R.string.settings_map_borders_desc),
                    enabled = userPreferences.showMapBorders,
                    onToggle = {
                        viewModel.processIntent(SettingsContract.Intent.UpdateMapBordersEnabled(it))
                    },
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_cache_stats)) {
                CacheStatisticsCard(
                    stats = cacheStats,
                    isLoading = isLoading,
                    onClearCache = { showClearCacheDialog = true },
                    onRefresh = { viewModel.processIntent(SettingsContract.Intent.LoadCacheStats) },
                )
            }

            // About Section
            SettingsSection(title = stringResource(R.string.settings_section_about)) {
                AboutSection(onOpenLicenses = onOpenLicenses)
            }
        }
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheDialog) {
        ClearCacheDialog(
            onConfirm = {
                showClearCacheDialog = false
                viewModel.processIntent(SettingsContract.Intent.ClearCache)
            },
            onDismiss = { showClearCacheDialog = false },
        )
    }
}

/**
 * Section header with title.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .semantics { heading() },
        )
        content()
    }
}

/**
 * Cache policy selector with radio buttons.
 */
@Composable
private fun CachePolicySelector(
    selectedPolicy: CachePolicy,
    onPolicySelected: (CachePolicy) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            CachePolicy.entries.filter { it != CachePolicy.FORCE_REFRESH }.forEachIndexed { index, policy ->
                CachePolicyOption(
                    policy = policy,
                    isSelected = policy == selectedPolicy,
                    onSelect = { onPolicySelected(policy) },
                )
                if (index < CachePolicy.entries.filter { it != CachePolicy.FORCE_REFRESH }.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

/**
 * Individual cache policy option with radio button.
 */
@Composable
private fun CachePolicyOption(
    policy: CachePolicy,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null, // Handled by Row's selectable
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = policy.displayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = policy.description(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * User-friendly cache policy display names.
 */
@Composable
private fun CachePolicy.displayName(): String = when (this) {
    CachePolicy.CACHE_FIRST -> stringResource(R.string.cache_policy_cache_first)
    CachePolicy.NETWORK_FIRST -> stringResource(R.string.cache_policy_network_first)
    CachePolicy.CACHE_ONLY -> stringResource(R.string.cache_policy_cache_only)
    CachePolicy.FORCE_REFRESH -> stringResource(R.string.cache_policy_force_refresh)
}

@Composable
private fun CachePolicy.description(): String = when (this) {
    CachePolicy.CACHE_FIRST -> stringResource(R.string.cache_policy_cache_first_desc)
    CachePolicy.NETWORK_FIRST -> stringResource(R.string.cache_policy_network_first_desc)
    CachePolicy.CACHE_ONLY -> stringResource(R.string.cache_policy_cache_only_desc)
    CachePolicy.FORCE_REFRESH -> stringResource(R.string.cache_policy_force_refresh_desc)
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == selectedMode,
                            onClick = { onModeSelected(mode) },
                            role = Role.RadioButton,
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = mode == selectedMode, onClick = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.displayName(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = mode.description(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (index < ThemeMode.entries.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeMode.displayName(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
    ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
}

@Composable
private fun ThemeMode.description(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system_desc)
    ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light_desc)
    ThemeMode.DARK -> stringResource(R.string.theme_mode_dark_desc)
}

/**
 * Material You dynamic colors (Android 12+). When off, uses the app Refined Explorer palette.
 */
@Composable
private fun DynamicColorSwitch(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_dynamic_colors),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.settings_dynamic_colors_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun FeatureToggleSwitch(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun OfflineModeSwitch(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_offline_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.settings_offline_mode_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

/**
 * Cache statistics display card.
 */
@Composable
private fun CacheStatisticsCard(
    stats: CacheStats,
    isLoading: Boolean,
    onClearCache: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                // Entry Count
                StatisticRow(
                    label = stringResource(R.string.settings_cached_countries),
                    value = "${stats.entryCount}",
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatisticRow(
                    label = stringResource(R.string.settings_estimated_size),
                    value = formatSize(stats.estimatedSizeKB),
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatisticRow(
                    label = stringResource(R.string.settings_oldest_entry),
                    value = formatAge(stats.oldestEntryAgeMs),
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Clear Cache Button
                TextButton(
                    onClick = onClearCache,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(stringResource(R.string.settings_clear_cache))
                }
            }
        }
    }
}

/**
 * Individual statistic row.
 */
@Composable
private fun StatisticRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Formats cache size from KB to human-readable format.
 */
@Composable
private fun formatSize(sizeKB: Int): String = when {
    sizeKB < 1024 -> stringResource(R.string.settings_size_kb, sizeKB)
    else -> stringResource(R.string.settings_size_mb, sizeKB / 1024.0)
}

@Composable
private fun formatAge(ageMs: Long): String {
    if (ageMs == 0L) return stringResource(R.string.settings_no_data)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMs)
    val hours = TimeUnit.MILLISECONDS.toHours(ageMs)
    val days = TimeUnit.MILLISECONDS.toDays(ageMs)

    return when {
        days > 0 -> pluralStringResource(R.plurals.settings_age_days, days.toInt(), days)
        hours > 0 -> pluralStringResource(R.plurals.settings_age_hours, hours.toInt(), hours)
        minutes > 0 -> pluralStringResource(R.plurals.settings_age_minutes, minutes.toInt(), minutes)
        else -> stringResource(R.string.settings_age_just_now)
    }
}

/**
 * About section with app information.
 */
@Composable
private fun AboutSection(onOpenLicenses: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = stringResource(R.string.settings_app_name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = stringResource(R.string.settings_version, "1.0.0"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.settings_features_header),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            val features = listOf(
                stringResource(R.string.settings_feature_cache_policy),
                stringResource(R.string.settings_feature_offline),
                stringResource(R.string.settings_feature_refresh),
                stringResource(R.string.settings_feature_search),
                stringResource(R.string.settings_feature_cache_age),
                stringResource(R.string.settings_feature_errors),
            )

            features.forEach { feature ->
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.settings_data_attribution_header),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = stringResource(R.string.settings_data_attribution_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TextButton(
                onClick = onOpenLicenses,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.settings_oss_licenses))
            }
        }
    }
}

/**
 * Confirmation dialog for clearing cache.
 */
@Composable
private fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.settings_clear_cache_title))
        },
        text = {
            Text(stringResource(R.string.settings_clear_cache_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_clear))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}
