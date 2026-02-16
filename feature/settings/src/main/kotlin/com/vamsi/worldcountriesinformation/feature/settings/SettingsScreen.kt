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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.datastore.CachePolicy
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
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    val cacheStats by viewModel.cacheStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showClearCacheDialog by remember { mutableStateOf(false) }

    // Show error messages in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            SnapNotify.showError(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cache Policy Section
            SettingsSection(title = "Cache Policy") {
                CachePolicySelector(
                    selectedPolicy = userPreferences.cachePolicy,
                    onPolicySelected = { viewModel.updateCachePolicy(it) }
                )
            }

            // Offline Mode Section
            SettingsSection(title = "Network") {
                OfflineModeSwitch(
                    enabled = userPreferences.offlineMode,
                    onToggle = { viewModel.updateOfflineMode(it) }
                )
            }

            // Cache Statistics Section
            SettingsSection(title = "Cache Statistics") {
                CacheStatisticsCard(
                    stats = cacheStats,
                    isLoading = isLoading,
                    onClearCache = { showClearCacheDialog = true },
                    onRefresh = { viewModel.loadCacheStatistics() }
                )
            }

            // About Section
            SettingsSection(title = "About") {
                AboutSection()
            }
        }
    }

    // Clear Cache Confirmation Dialog
    if (showClearCacheDialog) {
        ClearCacheDialog(
            onConfirm = {
                showClearCacheDialog = false
                viewModel.clearCache()
            },
            onDismiss = { showClearCacheDialog = false }
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
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
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
        ) {
            CachePolicy.entries.forEachIndexed { index, policy ->
                CachePolicyOption(
                    policy = policy,
                    isSelected = policy == selectedPolicy,
                    onSelect = { onPolicySelected(policy) }
                )
                if (index < CachePolicy.entries.size - 1) {
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
                role = Role.RadioButton
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null // Handled by Row's selectable
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = policy.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = policy.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Extension property for user-friendly cache policy display names.
 */
private val CachePolicy.displayName: String
    get() = when (this) {
        CachePolicy.CACHE_FIRST -> "Cache First"
        CachePolicy.NETWORK_FIRST -> "Network First"
        CachePolicy.CACHE_ONLY -> "Cache Only"
        CachePolicy.NETWORK_ONLY -> "Network Only"
    }

/**
 * Extension property for cache policy descriptions.
 */
private val CachePolicy.description: String
    get() = when (this) {
        CachePolicy.CACHE_FIRST -> "Use cached data when available, fetch from network if needed"
        CachePolicy.NETWORK_FIRST -> "Always fetch fresh data from network, fall back to cache on error"
        CachePolicy.CACHE_ONLY -> "Only use cached data, never make network requests"
        CachePolicy.NETWORK_ONLY -> "Always fetch from network, ignore cache completely"
    }

/**
 * Offline mode toggle switch.
 */
@Composable
private fun OfflineModeSwitch(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offline Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "When enabled, the app will only use cached data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
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
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Entry Count
                StatisticRow(
                    label = "Cached Countries",
                    value = "${stats.entryCount}"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Cache Size
                StatisticRow(
                    label = "Estimated Size",
                    value = formatSize(stats.estimatedSizeKB)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Cache Age
                StatisticRow(
                    label = "Oldest Entry",
                    value = formatAge(stats.oldestEntryAgeMs)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Clear Cache Button
                TextButton(
                    onClick = onClearCache,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Clear All Cache")
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Formats cache size from KB to human-readable format.
 */
private fun formatSize(sizeKB: Int): String {
    return when {
        sizeKB < 1024 -> "$sizeKB KB"
        else -> {
            val sizeMB = sizeKB / 1024.0
            "%.2f MB".format(sizeMB)
        }
    }
}

/**
 * Formats age in milliseconds to human-readable format.
 */
private fun formatAge(ageMs: Long): String {
    if (ageMs == 0L) return "No data"

    val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMs)
    val hours = TimeUnit.MILLISECONDS.toHours(ageMs)
    val days = TimeUnit.MILLISECONDS.toDays(ageMs)

    return when {
        days > 0 -> "$days ${if (days == 1L) "day" else "days"} ago"
        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        minutes > 0 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        else -> "Just now"
    }
}

/**
 * About section with app information.
 */
@Composable
private fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "World Countries Information",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Features:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            val features = listOf(
                "• Cache policy management",
                "• Offline mode support",
                "• Pull-to-refresh functionality",
                "• Search with debouncing",
                "• Cache age indicators",
                "• Comprehensive error handling"
            )

            features.forEach { feature ->
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                contentDescription = null
            )
        },
        title = {
            Text("Clear All Cache?")
        },
        text = {
            Text(
                "This will delete all cached country data. " +
                    "You'll need an internet connection to reload the data."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
