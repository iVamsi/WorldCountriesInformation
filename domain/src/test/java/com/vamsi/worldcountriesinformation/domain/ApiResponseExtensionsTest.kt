package com.vamsi.worldcountriesinformation.domain

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.data
import com.vamsi.worldcountriesinformation.domain.core.exception
import com.vamsi.worldcountriesinformation.domain.core.failed
import com.vamsi.worldcountriesinformation.domain.core.flatMap
import com.vamsi.worldcountriesinformation.domain.core.getOrElse
import com.vamsi.worldcountriesinformation.domain.core.getOrNull
import com.vamsi.worldcountriesinformation.domain.core.isLoading
import com.vamsi.worldcountriesinformation.domain.core.map
import com.vamsi.worldcountriesinformation.domain.core.onError
import com.vamsi.worldcountriesinformation.domain.core.onLoading
import com.vamsi.worldcountriesinformation.domain.core.onSuccess
import com.vamsi.worldcountriesinformation.domain.core.succeeded
import com.vamsi.worldcountriesinformation.domain.core.successOr
import com.vamsi.worldcountriesinformation.domain.core.toApiResponse
import com.vamsi.worldcountriesinformation.domain.core.toResult
import com.vamsi.worldcountriesinformation.domain.core.toResultOrNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * Unit tests for ApiResponse extensions and Result conversion functions.
 *
 * Tests the enhanced ApiResponse API including:
 * - Extension properties (succeeded, failed, isLoading, data, exception)
 * - Extension functions (map, flatMap, onSuccess, onError, onLoading)
 * - Result<T> conversion (toApiResponse, toResult, toResultOrNull)
 */
class ApiResponseExtensionsTest {

    // ========================================================================
    // Extension Properties Tests
    // ========================================================================

    @Test
    fun `succeeded returns true for Success with non-null data`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.succeeded, equalTo(true))
    }

    @Test
    fun `succeeded returns false for Error`() {
        val response: ApiResponse<String> = ApiResponse.Error(Exception())
        assertThat(response.succeeded, equalTo(false))
    }

    @Test
    fun `succeeded returns false for Loading`() {
        val response: ApiResponse<String> = ApiResponse.Loading
        assertThat(response.succeeded, equalTo(false))
    }

    @Test
    fun `failed returns true for Error`() {
        val response: ApiResponse<String> = ApiResponse.Error(Exception())
        assertThat(response.failed, equalTo(true))
    }

    @Test
    fun `failed returns false for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.failed, equalTo(false))
    }

    @Test
    fun `isLoading returns true for Loading`() {
        val response: ApiResponse<String> = ApiResponse.Loading
        assertThat(response.isLoading, equalTo(true))
    }

    @Test
    fun `isLoading returns false for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.isLoading, equalTo(false))
    }

    @Test
    fun `data returns value for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.data, equalTo("data"))
    }

    @Test
    fun `data returns null for Error`() {
        val response: ApiResponse<String> = ApiResponse.Error(Exception())
        assertThat(response.data, nullValue())
    }

    @Test
    fun `exception returns value for Error`() {
        val ex = Exception("test")
        val response: ApiResponse<String> = ApiResponse.Error(ex)
        assertThat(response.exception, equalTo(ex))
    }

    @Test
    fun `exception returns null for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.exception, nullValue())
    }

    // ========================================================================
    // Extension Functions Tests
    // ========================================================================

    @Test
    fun `successOr returns data for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.successOr("fallback"), equalTo("data"))
    }

    @Test
    fun `successOr returns fallback for Error`() {
        val response: ApiResponse<String> = ApiResponse.Error(Exception())
        assertThat(response.successOr("fallback"), equalTo("fallback"))
    }

    @Test
    fun `getOrElse returns data for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        val result = response.getOrElse { "fallback" }
        assertThat(result, equalTo("data"))
    }

    @Test
    fun `getOrElse returns fallback for Error`() {
        val response: ApiResponse<String> = ApiResponse.Error(Exception("test"))
        val result = response.getOrElse { exception ->
            assertThat(exception.message, equalTo("test"))
            "fallback"
        }
        assertThat(result, equalTo("fallback"))
    }

    @Test
    fun `getOrElse returns null for Loading`() {
        val response: ApiResponse<String> = ApiResponse.Loading
        val result = response.getOrElse { "fallback" }
        assertThat(result, nullValue())
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        assertThat(response.getOrNull(), equalTo("data"))
    }

    @Test
    fun `getOrNull returns null for Error`() {
        val response: ApiResponse<String> = ApiResponse.Error(Exception())
        assertThat(response.getOrNull(), nullValue())
    }

    @Test
    fun `map transforms Success data`() {
        val response: ApiResponse<Int> = ApiResponse.Success(42)
        val mapped = response.map { it.toString() }
        
        assertThat(mapped, instanceOf(ApiResponse.Success::class.java))
        assertThat((mapped as ApiResponse.Success).data, equalTo("42"))
    }

    @Test
    fun `map preserves Error`() {
        val ex = Exception("test")
        val response: ApiResponse<Int> = ApiResponse.Error(ex)
        val mapped = response.map { it.toString() }
        
        assertThat(mapped, instanceOf(ApiResponse.Error::class.java))
        assertThat((mapped as ApiResponse.Error).exception, equalTo(ex))
    }

    @Test
    fun `map preserves Loading`() {
        val response: ApiResponse<Int> = ApiResponse.Loading
        val mapped = response.map { it.toString() }
        
        assertThat(mapped, instanceOf(ApiResponse.Loading::class.java))
    }

    @Test
    fun `flatMap transforms Success to Success`() {
        val response: ApiResponse<Int> = ApiResponse.Success(42)
        val transformed = response.flatMap { value ->
            ApiResponse.Success(value * 2)
        }
        
        assertThat(transformed, instanceOf(ApiResponse.Success::class.java))
        assertThat((transformed as ApiResponse.Success).data, equalTo(84))
    }

    @Test
    fun `flatMap transforms Success to Error`() {
        val response: ApiResponse<Int> = ApiResponse.Success(-1)
        val transformed = response.flatMap { value ->
            if (value > 0) ApiResponse.Success(value * 2)
            else ApiResponse.Error(Exception("Negative value"))
        }
        
        assertThat(transformed, instanceOf(ApiResponse.Error::class.java))
    }

    @Test
    fun `flatMap preserves Error`() {
        val ex = Exception("test")
        val response: ApiResponse<Int> = ApiResponse.Error(ex)
        val transformed = response.flatMap { ApiResponse.Success(it * 2) }
        
        assertThat(transformed, instanceOf(ApiResponse.Error::class.java))
        assertThat((transformed as ApiResponse.Error).exception, equalTo(ex))
    }

    @Test
    fun `onSuccess executes action for Success`() {
        var executed = false
        val response: ApiResponse<String> = ApiResponse.Success("data")
        
        response.onSuccess { data ->
            executed = true
            assertThat(data, equalTo("data"))
        }
        
        assertThat(executed, equalTo(true))
    }

    @Test
    fun `onSuccess does not execute for Error`() {
        var executed = false
        val response: ApiResponse<String> = ApiResponse.Error(Exception())
        
        response.onSuccess { executed = true }
        
        assertThat(executed, equalTo(false))
    }

    @Test
    fun `onError executes action for Error`() {
        var executed = false
        val ex = Exception("test")
        val response: ApiResponse<String> = ApiResponse.Error(ex)
        
        response.onError { exception ->
            executed = true
            assertThat(exception, equalTo(ex))
        }
        
        assertThat(executed, equalTo(true))
    }

    @Test
    fun `onError does not execute for Success`() {
        var executed = false
        val response: ApiResponse<String> = ApiResponse.Success("data")
        
        response.onError { executed = true }
        
        assertThat(executed, equalTo(false))
    }

    @Test
    fun `onLoading executes action for Loading`() {
        var executed = false
        val response: ApiResponse<String> = ApiResponse.Loading
        
        response.onLoading { executed = true }
        
        assertThat(executed, equalTo(true))
    }

    @Test
    fun `onLoading does not execute for Success`() {
        var executed = false
        val response: ApiResponse<String> = ApiResponse.Success("data")
        
        response.onLoading { executed = true }
        
        assertThat(executed, equalTo(false))
    }

    @Test
    fun `chained callbacks execute in order`() {
        val log = mutableListOf<String>()
        
        ApiResponse.Success("data")
            .onLoading { log.add("loading") }
            .onSuccess { log.add("success: $it") }
            .onError { log.add("error") }
        
        assertThat(log, equalTo(listOf("success: data")))
    }

    // ========================================================================
    // Result Conversion Tests
    // ========================================================================

    @Test
    fun `Result success converts to ApiResponse Success`() {
        val result: Result<String> = Result.success("data")
        val response = result.toApiResponse()
        
        assertThat(response, instanceOf(ApiResponse.Success::class.java))
        assertThat((response as ApiResponse.Success).data, equalTo("data"))
    }

    @Test
    fun `Result failure converts to ApiResponse Error`() {
        val ex = Exception("test")
        val result: Result<String> = Result.failure(ex)
        val response = result.toApiResponse()
        
        assertThat(response, instanceOf(ApiResponse.Error::class.java))
        assertThat((response as ApiResponse.Error).exception, equalTo(ex))
    }

    @Test
    fun `Result failure with Throwable converts to ApiResponse Error with Exception wrapper`() {
        val throwable = Throwable("test")
        val result: Result<String> = Result.failure(throwable)
        val response = result.toApiResponse()
        
        assertThat(response, instanceOf(ApiResponse.Error::class.java))
        assertThat((response as ApiResponse.Error).exception, instanceOf(Exception::class.java))
    }

    @Test
    fun `ApiResponse Success converts to Result success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        val result = response.toResult()
        
        assertThat(result.isSuccess, equalTo(true))
        assertThat(result.getOrNull(), equalTo("data"))
    }

    @Test
    fun `ApiResponse Error converts to Result failure`() {
        val ex = Exception("test")
        val response: ApiResponse<String> = ApiResponse.Error(ex)
        val result = response.toResult()
        
        assertThat(result.isFailure, equalTo(true))
        assertThat(result.exceptionOrNull(), equalTo(ex))
    }

    @Test
    fun `ApiResponse Loading converts to Result failure with IllegalStateException`() {
        val response: ApiResponse<String> = ApiResponse.Loading
        val result = response.toResult()
        
        assertThat(result.isFailure, equalTo(true))
        assertThat(result.exceptionOrNull(), instanceOf(IllegalStateException::class.java))
    }

    @Test
    fun `toResultOrNull returns Result for Success`() {
        val response: ApiResponse<String> = ApiResponse.Success("data")
        val result = response.toResultOrNull()
        
        assertThat(result, instanceOf(Result::class.java))
        assertThat(result?.getOrNull(), equalTo("data"))
    }

    @Test
    fun `toResultOrNull returns Result for Error`() {
        val ex = Exception("test")
        val response: ApiResponse<String> = ApiResponse.Error(ex)
        val result = response.toResultOrNull()
        
        assertThat(result, instanceOf(Result::class.java))
        assertThat(result?.exceptionOrNull(), equalTo(ex))
    }

    @Test
    fun `toResultOrNull returns null for Loading`() {
        val response: ApiResponse<String> = ApiResponse.Loading
        val result = response.toResultOrNull()
        
        assertThat(result, nullValue())
    }

    // ========================================================================
    // Integration Tests
    // ========================================================================

    @Test
    fun `complex transformation chain with Result integration`() {
        val result: Result<Int> = Result.success(21)
        
        val finalResponse = result
            .toApiResponse()
            .map { it * 2 }
            .flatMap { value ->
                if (value == 42) ApiResponse.Success("The answer")
                else ApiResponse.Error(Exception("Wrong answer"))
            }
            .onSuccess { data ->
                assertThat(data, equalTo("The answer"))
            }
        
        assertThat(finalResponse.succeeded, equalTo(true))
        assertThat(finalResponse.data, equalTo("The answer"))
    }

    @Test
    fun `Result failure propagates through chain`() {
        val result: Result<Int> = Result.failure(Exception("Initial error"))
        
        val finalResponse = result
            .toApiResponse()
            .map { it * 2 }
            .flatMap { ApiResponse.Success(it.toString()) }
            .onError { exception ->
                assertThat(exception.message, equalTo("Initial error"))
            }
        
        assertThat(finalResponse.failed, equalTo(true))
    }

    @Test
    fun `round-trip conversion preserves data`() {
        val originalResponse: ApiResponse<String> = ApiResponse.Success("test")
        
        val result = originalResponse.toResult()
        val finalResponse = result.toApiResponse()
        
        assertThat(finalResponse, instanceOf(ApiResponse.Success::class.java))
        assertThat((finalResponse as ApiResponse.Success).data, equalTo("test"))
    }
}
