package com.vamsi.worldcountriesinformation.domain

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.UseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UseCaseTest {

    @Test
    fun `invoke maps non cancellation exception to Error`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val useCase = object : UseCase<Unit, String>(dispatcher) {
            override suspend fun execute(parameters: Unit): String {
                error("boom")
            }
        }
        val result = useCase(Unit)
        assertTrue(result is ApiResponse.Error)
        val err = result as ApiResponse.Error
        assertTrue(err.exception.message == "boom")
    }

    @Test
    fun `invoke rethrows CancellationException from execute`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val useCase = object : UseCase<Unit, String>(dispatcher) {
            override suspend fun execute(parameters: Unit): String {
                throw CancellationException("cancelled")
            }
        }
        try {
            useCase(Unit)
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertTrue(e.message == "cancelled")
        }
    }

    @Test
    fun `invoke propagates cancellation when deferred cancelled during execute`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val useCase = object : UseCase<Unit, String>(dispatcher) {
            override suspend fun execute(parameters: Unit): String {
                delay(Long.MAX_VALUE)
                return "never"
            }
        }
        val deferred = async { useCase(Unit) }
        advanceUntilIdle()
        deferred.cancel()
        try {
            deferred.await()
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            // Expected: cancellation must not be turned into ApiResponse.Error
        }
    }
}
