package com.vamsi.worldcountriesinformation.domain.core

import com.vamsi.worldcountriesinformation.domain.core.CurrentState.Success

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class CurrentState<out R> {

    data class Success<out T>(val data: T) : CurrentState<T>()
    data class Error(val exception: Exception) : CurrentState<Nothing>()
    object Loading : CurrentState<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * `true` if [CurrentState] is of type [Success] & holds non-null [Success.data].
 */
val CurrentState<*>.succeeded
    get() = this is Success && data != null

fun <T> CurrentState<T>.successOr(fallback: T): T {
    return (this as? Success<T>)?.data ?: fallback
}

val <T> CurrentState<T>.data: T?
    get() = (this as? Success)?.data
