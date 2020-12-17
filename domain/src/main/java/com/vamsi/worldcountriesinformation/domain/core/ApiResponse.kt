package com.vamsi.worldcountriesinformation.domain.core

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse.Success

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ApiResponse<out R> {

    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val exception: Exception) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * `true` if [ApiResponse] is of type [Success] & holds non-null [Success.data].
 */
val ApiResponse<*>.succeeded
    get() = this is Success && data != null

fun <T> ApiResponse<T>.successOr(fallback: T): T {
    return (this as? Success<T>)?.data ?: fallback
}

val <T> ApiResponse<T>.data: T?
    get() = (this as? Success)?.data
