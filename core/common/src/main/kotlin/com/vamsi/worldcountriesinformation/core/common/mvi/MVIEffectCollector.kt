package com.vamsi.worldcountriesinformation.core.common.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects effects from an MVI ViewModel in a Compose-safe manner.
 *
 * This extension function automatically handles the lifecycle of effect collection
 * and ensures effects are only collected when the composable is active.
 *
 * ## Usage
 *
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyMviViewModel = hiltViewModel()) {
 *     val state by viewModel.state.collectAsStateWithLifecycle()
 *
 *     viewModel.effect.collectAsEffect { effect ->
 *         when (effect) {
 *             is MyEffect.NavigateToDetails -> navController.navigate(...)
 *             is MyEffect.ShowToast -> showToast(effect.message)
 *         }
 *     }
 *
 *     // Render UI based on state
 * }
 * ```
 *
 * ## Lifecycle Safety
 *
 * - Effects are collected in a [LaunchedEffect] with a stable key
 * - Collection stops when composable leaves composition
 * - Collection restarts on recomposition with same effect flow
 *
 * @param context Optional [CoroutineContext] for collection (default: EmptyCoroutineContext)
 * @param block Suspend function to handle each effect
 *
 * @since 1.0.0
 */
@Composable
fun <T> Flow<T>.collectAsEffect(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend (T) -> Unit,
) {
    LaunchedEffect(Unit) {
        this@collectAsEffect
            .collect { value ->
                block(value)
            }
    }
}

/**
 * Alternative version with explicit lifecycle owner.
 *
 * Use this when you need more control over the collection lifecycle.
 *
 * @param key Unique key for LaunchedEffect (restarts collection when key changes)
 * @param context Optional [CoroutineContext] for collection
 * @param block Suspend function to handle each effect
 */
@Composable
fun <T> Flow<T>.collectAsEffect(
    key: Any?,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend (T) -> Unit,
) {
    LaunchedEffect(key) {
        this@collectAsEffect
            .collect { value ->
                block(value)
            }
    }
}
