package com.vamsi.worldcountriesinformation.domain.core

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse.Success

/**
 * A generic sealed class that represents the state of an API operation.
 *
 * This class provides three states:
 * - [Loading]: Operation in progress
 * - [Success]: Operation completed successfully with data
 * - [Error]: Operation failed with an exception
 *
 * ## Advantages over Kotlin's Result<T>
 *
 * While Kotlin's `Result<T>` is excellent for representing success/failure,
 * `ApiResponse<T>` adds a crucial [Loading] state that is essential for:
 * - UI loading indicators
 * - Progress tracking
 * - Differentiating "no data yet" from "failed to load"
 * - Reactive Flow-based operations
 *
 * ## Integration with Result<T>
 *
 * Use extension functions to convert between `Result<T>` and `ApiResponse<T>`:
 * ```kotlin
 * val result: Result<String> = Result.success("data")
 * val apiResponse: ApiResponse<String> = result.toApiResponse()
 * ```
 *
 * ## Usage Example
 *
 * ```kotlin
 * fun loadData(): Flow<ApiResponse<Data>> = flow {
 *     emit(ApiResponse.Loading)
 *     try {
 *         val data = api.fetchData()
 *         emit(ApiResponse.Success(data))
 *     } catch (e: Exception) {
 *         emit(ApiResponse.Error(e))
 *     }
 * }
 * ```
 *
 * @param R The type of data held in [Success] state
 *
 * @see Result for Kotlin's standard success/failure type
 * @see Loading for in-progress state
 * @see Success for completed state with data
 * @see Error for failed state with exception
 */
sealed class ApiResponse<out R> {

    /**
     * Represents a successful operation with resulting data.
     *
     * @param data The result data of type [T]
     */
    data class Success<out T>(val data: T) : ApiResponse<T>()

    /**
     * Represents a failed operation with an exception.
     *
     * @param exception The exception that caused the failure
     */
    data class Error(val exception: Exception) : ApiResponse<Nothing>()

    /**
     * Represents an operation in progress.
     *
     * Use this state to show loading indicators or progress bars in UI.
     */
    data object Loading : ApiResponse<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * Returns `true` if [ApiResponse] is [Success] and holds non-null data.
 *
 * Example:
 * ```kotlin
 * val response: ApiResponse<String> = ApiResponse.Success("data")
 * if (response.succeeded) {
 *     println("Success!")
 * }
 * ```
 */
val ApiResponse<*>.succeeded: Boolean
    get() = this is Success && data != null

/**
 * Returns `true` if [ApiResponse] is [Error].
 */
val ApiResponse<*>.failed: Boolean
    get() = this is ApiResponse.Error

/**
 * Returns `true` if [ApiResponse] is [Loading].
 */
val ApiResponse<*>.isLoading: Boolean
    get() = this is ApiResponse.Loading

/**
 * Returns the data if [Success], or null otherwise.
 *
 * Example:
 * ```kotlin
 * val response: ApiResponse<String> = ApiResponse.Success("data")
 * val data = response.data // "data"
 * ```
 */
val <T> ApiResponse<T>.data: T?
    get() = (this as? Success)?.data

/**
 * Returns the exception if [Error], or null otherwise.
 */
val ApiResponse<*>.exception: Exception?
    get() = (this as? ApiResponse.Error)?.exception

/**
 * Returns the data if [Success], or the provided fallback value otherwise.
 *
 * Example:
 * ```kotlin
 * val response: ApiResponse<String> = ApiResponse.Error(Exception())
 * val data = response.successOr("default") // "default"
 * ```
 */
fun <T> ApiResponse<T>.successOr(fallback: T): T {
    return (this as? Success<T>)?.data ?: fallback
}

/**
 * Returns the data if [Success], or the result of [onFailure] if [Error].
 * Returns null if [Loading].
 *
 * Example:
 * ```kotlin
 * val data = response.getOrElse { exception ->
 *     println("Error: ${exception.message}")
 *     "fallback"
 * }
 * ```
 */
inline fun <T> ApiResponse<T>.getOrElse(onFailure: (Exception) -> T): T? {
    return when (this) {
        is Success -> data
        is ApiResponse.Error -> onFailure(exception)
        is ApiResponse.Loading -> null
    }
}

/**
 * Returns the data if [Success], or null if [Error] or [Loading].
 *
 * Example:
 * ```kotlin
 * val data = response.getOrNull() // null if not Success
 * ```
 */
fun <T> ApiResponse<T>.getOrNull(): T? = data

/**
 * Transforms [Success] data using [transform], or returns the original [Error]/[Loading].
 *
 * Example:
 * ```kotlin
 * val intResponse: ApiResponse<Int> = ApiResponse.Success(42)
 * val stringResponse: ApiResponse<String> = intResponse.map { it.toString() }
 * // ApiResponse.Success("42")
 * ```
 */
inline fun <T, R> ApiResponse<T>.map(transform: (T) -> R): ApiResponse<R> {
    return when (this) {
        is Success -> Success(transform(data))
        is ApiResponse.Error -> this
        is ApiResponse.Loading -> this
    }
}

/**
 * Flat-maps [Success] data using [transform], or returns the original [Error]/[Loading].
 *
 * Example:
 * ```kotlin
 * val response: ApiResponse<Int> = ApiResponse.Success(42)
 * val transformed = response.flatMap { value ->
 *     if (value > 0) ApiResponse.Success(value * 2)
 *     else ApiResponse.Error(Exception("Negative value"))
 * }
 * ```
 */
inline fun <T, R> ApiResponse<T>.flatMap(transform: (T) -> ApiResponse<R>): ApiResponse<R> {
    return when (this) {
        is Success -> transform(data)
        is ApiResponse.Error -> this
        is ApiResponse.Loading -> this
    }
}

/**
 * Executes [action] if [Success], and returns the original response.
 *
 * Example:
 * ```kotlin
 * response.onSuccess { data ->
 *     println("Success: $data")
 * }
 * ```
 */
inline fun <T> ApiResponse<T>.onSuccess(action: (T) -> Unit): ApiResponse<T> {
    if (this is Success) action(data)
    return this
}

/**
 * Executes [action] if [Error], and returns the original response.
 *
 * Example:
 * ```kotlin
 * response.onError { exception ->
 *     println("Error: ${exception.message}")
 * }
 * ```
 */
inline fun <T> ApiResponse<T>.onError(action: (Exception) -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Error) action(exception)
    return this
}

/**
 * Executes [action] if [Loading], and returns the original response.
 *
 * Example:
 * ```kotlin
 * response.onLoading {
 *     println("Loading...")
 * }
 * ```
 */
inline fun <T> ApiResponse<T>.onLoading(action: () -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Loading) action()
    return this
}

/**
 * Converts Kotlin's [Result]<T> to [ApiResponse]<T>.
 *
 * - [Result.success] → [Success]
 * - [Result.failure] → [Error]
 *
 * Note: [Result] doesn't have a loading state.
 *
 * Example:
 * ```kotlin
 * val result: Result<String> = Result.success("data")
 * val response: ApiResponse<String> = result.toApiResponse()
 * // ApiResponse.Success("data")
 * ```
 */
fun <T> Result<T>.toApiResponse(): ApiResponse<T> {
    return fold(
        onSuccess = { Success(it) },
        onFailure = {
            ApiResponse.Error(
                when (it) {
                    is Exception -> it
                    else -> Exception(it)
                }
            )
        }
    )
}

/**
 * Converts [ApiResponse]<T> to Kotlin's [Result]<T>.
 *
 * - [Success] → [Result.success]
 * - [Error] → [Result.failure]
 * - [Loading] → [Result.failure] with [IllegalStateException]
 *
 * Note: [Loading] state is converted to a failure since [Result] has no loading state.
 *
 * Example:
 * ```kotlin
 * val response: ApiResponse<String> = ApiResponse.Success("data")
 * val result: Result<String> = response.toResult()
 * // Result.success("data")
 * ```
 */
fun <T> ApiResponse<T>.toResult(): Result<T> {
    return when (this) {
        is Success -> Result.success(data)
        is ApiResponse.Error -> Result.failure(exception)
        is ApiResponse.Loading -> Result.failure(
            IllegalStateException("Cannot convert Loading state to Result")
        )
    }
}

/**
 * Converts [ApiResponse]<T> to [Result]<T>, returning null for [Loading] state.
 *
 * Example:
 * ```kotlin
 * val response: ApiResponse<String> = ApiResponse.Loading
 * val result: Result<String>? = response.toResultOrNull()
 * // null
 * ```
 */
fun <T> ApiResponse<T>.toResultOrNull(): Result<T>? {
    return when (this) {
        is Success -> Result.success(data)
        is ApiResponse.Error -> Result.failure(exception)
        is ApiResponse.Loading -> null
    }
}
