package com.vamsi.worldcountriesinformation.domain.core

/**
 * A generic sealed interface representing the UI state.
 * This provides a single source of truth for UI state management.
 *
 * @param T The type of data when in success state
 */
sealed interface UiState<out T> {
    /**
     * Initial state before any operation
     */
    object Idle : UiState<Nothing>

    /**
     * Loading state during async operations
     */
    object Loading : UiState<Nothing>

    /**
     * Success state with data
     * @param data The successful result data
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * Error state with exception details
     * @param exception The exception that occurred
     * @param message User-friendly error message
     */
    data class Error(
        val exception: Exception,
        val message: String? = exception.message
    ) : UiState<Nothing>
}

/**
 * Extension to check if UiState is in Success state
 */
val UiState<*>.isSuccess: Boolean
    get() = this is UiState.Success

/**
 * Extension to check if UiState is in Loading state
 */
val UiState<*>.isLoading: Boolean
    get() = this is UiState.Loading

/**
 * Extension to check if UiState is in Error state
 */
val UiState<*>.isError: Boolean
    get() = this is UiState.Error

/**
 * Extension to get data from Success state or return fallback
 */
fun <T> UiState<T>.getDataOrNull(): T? {
    return (this as? UiState.Success)?.data
}

/**
 * Extension to get data from Success state or return fallback
 */
fun <T> UiState<T>.getDataOr(fallback: T): T {
    return (this as? UiState.Success)?.data ?: fallback
}
