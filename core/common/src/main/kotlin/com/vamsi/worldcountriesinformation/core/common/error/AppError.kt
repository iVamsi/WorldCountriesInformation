package com.vamsi.worldcountriesinformation.core.common.error

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import com.vamsi.worldcountriesinformation.core.common.R
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Canonical, UI-agnostic error model used across the app.
 *
 * ViewModels should expose `AppError` (not raw `Throwable` or hardcoded
 * English strings). The UI layer translates an `AppError` into a localized
 * string via [Context.message] (or via `stringResource(error.messageRes, ...)`)
 * so error copy participates in the regular resource/localization pipeline.
 */
sealed interface AppError {
    @get:StringRes
    val messageRes: Int

    val formatArgs: Array<Any> get() = emptyArray()

    data object Network : AppError {
        override val messageRes: Int = R.string.error_network
    }

    data object Timeout : AppError {
        override val messageRes: Int = R.string.error_timeout
    }

    data object NoCachedData : AppError {
        override val messageRes: Int = R.string.error_no_cached_data
    }

    data class NotFound(val identifier: String) : AppError {
        override val messageRes: Int = R.string.error_not_found
        override val formatArgs: Array<Any> get() = arrayOf(identifier)
    }

    /**
     * Caller-specified fallback when the cause is unknown. Lets each screen
     * pick a more specific message (e.g. "Failed to load countries" vs
     * "Failed to load country details") without inflating the AppError tree.
     */
    data class Generic(@get:StringRes override val messageRes: Int) : AppError

    data object Unknown : AppError {
        override val messageRes: Int = R.string.error_unknown
    }
}

/**
 * Map a [Throwable] to an [AppError]. Cooperative cancellation is preserved.
 *
 * String-matching on `message` mirrors what the existing ViewModels already do;
 * once domain layer surfaces typed exceptions we should match on those instead.
 */
fun Throwable.toAppError(fallback: AppError = AppError.Unknown): AppError {
    if (this is CancellationException) throw this
    val msg = message.orEmpty()
    return when {
        msg.contains("No cached data", ignoreCase = true) -> AppError.NoCachedData
        msg.contains("not found", ignoreCase = true) -> AppError.NotFound(identifier = "")
        this is SocketTimeoutException -> AppError.Timeout
        msg.contains("timeout", ignoreCase = true) -> AppError.Timeout
        this is UnknownHostException -> AppError.Network
        this is IOException -> AppError.Network
        msg.contains("network", ignoreCase = true) -> AppError.Network
        else -> fallback
    }
}

/** Localize the given [AppError] using the application's resources. */
fun Context.message(error: AppError): String =
    getString(error.messageRes, *error.formatArgs)

/**
 * Localize the given [AppError] from a [Resources] handle. Prefer this in Compose
 * via `LocalResources.current` so configuration changes invalidate properly.
 */
fun Resources.message(error: AppError): String =
    getString(error.messageRes, *error.formatArgs)
