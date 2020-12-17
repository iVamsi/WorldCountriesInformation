package com.vamsi.worldcountriesinformation.domain

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domain.core.FlowUseCase
import com.vamsi.worldcountriesinformation.tests_shared.MainCoroutineRule
import com.vamsi.worldcountriesinformation.tests_shared.runBlockingTest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class FlowUseCaseTest {

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val testDispatcher = coroutineRule.testDispatcher

    @Test
    fun `exception emits ApiResponse#Error`() = coroutineRule.runBlockingTest {
        val useCase = ExceptionUseCase(testDispatcher)
        val result = useCase(Unit)
        MatcherAssert.assertThat(result.first(), CoreMatchers.instanceOf(ApiResponse.Error::class.java))
    }

    class ExceptionUseCase(dispatcher: CoroutineDispatcher) : FlowUseCase<Unit, Unit>(dispatcher) {
        override fun execute(parameters: Unit): Flow<ApiResponse<Unit>> = flow {
            throw Exception("Test exception")
        }
    }
}