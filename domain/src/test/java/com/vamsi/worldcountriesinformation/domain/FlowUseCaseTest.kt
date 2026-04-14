package com.vamsi.worldcountriesinformation.domain

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.FlowUseCase
import com.vamsi.worldcountriesinformation.tests_shared.MainCoroutineRule
import com.vamsi.worldcountriesinformation.tests_shared.runBlockingTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class FlowUseCaseTest {

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val testDispatcher = coroutineRule.testDispatcher

    @Test
    fun `exception emits ApiResponse#Error`() = runBlockingTest {
        val useCase = ExceptionUseCase(testDispatcher)
        val result = useCase(Unit).first()
        MatcherAssert.assertThat(result, CoreMatchers.instanceOf(ApiResponse.Error::class.java))
    }

    @Test
    fun `cancellation is not mapped to ApiResponse Error`() = runTest {
        val useCase = CancellationUseCase(testDispatcher)
        try {
            useCase(Unit).first()
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            // Flow catch must rethrow cancellation
        }
    }

    class ExceptionUseCase(dispatcher: CoroutineDispatcher) : FlowUseCase<Unit, Unit>(dispatcher) {
        override fun execute(parameters: Unit): Flow<ApiResponse<Unit>> = flow {
            throw Exception("Test exception")
        }
    }

    private class CancellationUseCase(dispatcher: CoroutineDispatcher) :
        FlowUseCase<Unit, Unit>(dispatcher) {
        override fun execute(parameters: Unit): Flow<ApiResponse<Unit>> = flow {
            throw CancellationException("cancelled")
        }
    }
}
