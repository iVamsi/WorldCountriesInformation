@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.vamsi.worldcountriesinformation.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.designsystem.component.FactTile
import com.vamsi.worldcountriesinformation.domain.core.CachePolicy
import com.vamsi.worldcountriesinformation.domain.preferences.RefreshInterval
import com.vamsi.worldcountriesinformation.domain.preferences.ThemeMode
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

/**
 * Settings: cache policy, offline mode, appearance, feature toggles, cache statistics, and about.
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onDailyNotificationChanged: (Boolean) -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    SettingsScreenContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        onDailyNotificationChanged = onDailyNotificationChanged,
        onOpenLicenses = onOpenLicenses,
    )
}

@Composable
internal fun SettingsScreenContent(
    state: SettingsContract.State,
    onIntent: (SettingsContract.Intent) -> Unit,
    onNavigateBack: () -> Unit,
    onDailyNotificationChanged: (Boolean) -> Unit = {},
    onOpenLicenses: () -> Unit = {},
) {
    val prefs = state.userPreferences
    var showClearCacheDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.testTag(UiTestTags.SETTINGS_SCREEN),
        containerColor = Color.Transparent,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_cache_policy)) {
                    Column(Modifier.selectableGroup()) {
                        val policies = CachePolicy.entries.filter { it != CachePolicy.FORCE_REFRESH }
                        policies.forEachIndexed { index, policy ->
                            RadioRow(
                                title = policy.displayName(),
                                description = policy.description(),
                                selected = policy == prefs.cachePolicy,
                                onSelect = { onIntent(SettingsContract.Intent.UpdateCachePolicy(policy)) },
                            )
                            if (index < policies.lastIndex) RowDivider()
                        }
                    }
                    RowDivider()
                    Text(
                        text = stringResource(R.string.settings_refresh_interval),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                    )
                    Text(
                        text = stringResource(R.string.refresh_interval_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp),
                    )
                    Column(Modifier.selectableGroup()) {
                        RefreshInterval.entries.forEach { interval ->
                            RadioRow(
                                title = interval.displayName(),
                                description = null,
                                selected = interval == prefs.refreshInterval,
                                onSelect = { onIntent(SettingsContract.Intent.UpdateRefreshInterval(interval)) },
                            )
                        }
                    }
                    RowDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_offline_mode),
                        description = stringResource(R.string.settings_offline_mode_desc),
                        checked = prefs.offlineMode,
                        onCheckedChange = { onIntent(SettingsContract.Intent.UpdateOfflineMode(it)) },
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                    Column(Modifier.selectableGroup()) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            RadioRow(
                                title = mode.displayName(),
                                description = mode.description(),
                                selected = mode == prefs.themeMode,
                                onSelect = { onIntent(SettingsContract.Intent.UpdateThemeMode(mode)) },
                            )
                            if (index < ThemeMode.entries.lastIndex) RowDivider()
                        }
                    }
                    RowDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_dynamic_colors),
                        description = stringResource(R.string.settings_dynamic_colors_desc),
                        checked = prefs.useDynamicColor,
                        onCheckedChange = { onIntent(SettingsContract.Intent.UpdateUseDynamicColor(it)) },
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_section_features)) {
                    SwitchRow(
                        title = stringResource(R.string.settings_ai_summaries),
                        description = stringResource(R.string.settings_ai_summaries_desc),
                        checked = prefs.aiSummaryEnabled,
                        onCheckedChange = { onIntent(SettingsContract.Intent.UpdateAiSummaryEnabled(it)) },
                    )
                    RowDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_daily_notification),
                        description = stringResource(R.string.settings_daily_notification_desc),
                        checked = prefs.dailyNotificationEnabled,
                        onCheckedChange = {
                            onIntent(SettingsContract.Intent.UpdateDailyNotificationEnabled(it))
                            onDailyNotificationChanged(it)
                        },
                    )
                    RowDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_map_borders),
                        description = stringResource(R.string.settings_map_borders_desc),
                        checked = prefs.showMapBorders,
                        onCheckedChange = { onIntent(SettingsContract.Intent.UpdateMapBordersEnabled(it)) },
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_section_cache_stats), card = false) {
                    CacheStatistics(
                        stats = state.cacheStats,
                        isLoading = state.isLoading,
                        onClearCache = { showClearCacheDialog = true },
                    )
                }

                SettingsSection(title = stringResource(R.string.settings_section_about)) {
                    AboutSection(onOpenLicenses = onOpenLicenses)
                }
            }
        }
    }

    if (showClearCacheDialog) {
        ClearCacheDialog(
            onConfirm = {
                showClearCacheDialog = false
                onIntent(SettingsContract.Intent.ClearCache)
            },
            onDismiss = { showClearCacheDialog = false },
        )
    }
}

/** Section title over one low tonal card (or bare content when [card] is false). */
@Composable
private fun SettingsSection(
    title: String,
    card: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        if (card) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                content()
            }
        } else {
            content()
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

@Composable
private fun RadioRow(
    title: String,
    description: String?,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    )
}

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
private fun RefreshInterval.displayName(): String = when (this) {
    RefreshInterval.DAILY -> stringResource(R.string.refresh_interval_daily)
    RefreshInterval.WEEKLY -> stringResource(R.string.refresh_interval_weekly)
    RefreshInterval.MONTHLY -> stringResource(R.string.refresh_interval_monthly)
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

@Composable
private fun CacheStatistics(
    stats: CacheStats,
    isLoading: Boolean,
    onClearCache: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FactTile(
                    label = stringResource(R.string.settings_cached_countries),
                    value = stats.entryCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                FactTile(
                    label = stringResource(R.string.settings_estimated_size),
                    value = formatSize(stats.estimatedSizeKB),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FactTile(
                    label = stringResource(R.string.settings_last_checked),
                    value = formatAge(stats.lastCheckedAgeMs),
                    modifier = Modifier.weight(1f),
                )
                FactTile(
                    label = stringResource(R.string.settings_last_changed),
                    value = formatAge(stats.lastChangedAgeMs),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        TextButton(onClick = onClearCache, modifier = Modifier.align(Alignment.End)) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(stringResource(R.string.settings_clear_cache))
        }
    }
}

@Composable
private fun formatSize(sizeKB: Int): String = when {
    sizeKB < KB_PER_MB -> stringResource(R.string.settings_size_kb, sizeKB)
    else -> stringResource(R.string.settings_size_mb, sizeKB / KB_PER_MB.toDouble())
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

@Composable
private fun AboutSection(onOpenLicenses: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_app_name)) },
        supportingContent = { Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
    RowDivider()
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_data_attribution_header)) },
        supportingContent = { Text(stringResource(R.string.settings_data_attribution_body)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
    RowDivider()
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_oss_licenses)) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onOpenLicenses),
    )
}

@Composable
private fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) },
        title = { Text(stringResource(R.string.settings_clear_cache_title)) },
        text = { Text(stringResource(R.string.settings_clear_cache_message)) },
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

private val CONTENT_MAX_WIDTH = 640.dp
private const val KB_PER_MB = 1024

@PreviewLightDark
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    WorldCountriesTheme(dynamicColor = false) {
        SettingsScreenContent(
            state = SettingsContract.State(
                cacheStats = CacheStats(
                    entryCount = 250,
                    estimatedSizeKB = 512,
                    lastCheckedAgeMs = 7_200_000,
                    lastChangedAgeMs = 260_000_000,
                ),
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
