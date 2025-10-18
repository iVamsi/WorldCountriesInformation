package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.ApiResponse
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException

/**
 * Unit tests for [GetCountryByCodeUseCase].
 *
 * This test class verifies the behavior of the GetCountryByCodeUseCase including:
 * - Successful country retrieval
 * - Error handling
 * - Code normalization
 * - Input validation
 *
 * **Test Strategy:**
 * - Uses MockK for mocking dependencies
 * - Uses Kotlin Coroutines Test for testing coroutines
 * - Follows Given-When-Then pattern
 * - Tests both success and error scenarios
 *
 * @see GetCountryByCodeUseCase
 */
@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class GetCountryByCodeUseCaseTest {

    // Test dispatcher for controlling coroutine execution
    private val testDispatcher = StandardTestDispatcher()

    // Mock repository
    private lateinit var countriesRepository: CountriesRepository
    
    // System under test
    private lateinit var useCase: GetCountryByCodeUseCase

    /**
     * Test data: Sample country for testing
     */
    private val sampleCountry = Country(
        name = "United States",
        capital = "Washington, D.C.",
        languages = listOf(Language("English", "English")),
        twoLetterCode = "US",
        threeLetterCode = "USA",
        population = 331000000,
        region = "Americas",
        currencies = listOf(Currency("USD", "United States dollar", "$")),
        callingCode = "1",
        latitude = 38.0,
        longitude = -97.0
    )

    @Before
    fun setUp() {
        // Set main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Initialize mock repository
        countriesRepository = mockk()
        
        // Initialize use case with mock repository
        useCase = GetCountryByCodeUseCase(countriesRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        // Reset main dispatcher after tests
        Dispatchers.resetMain()
    }

    /**
     * Test: Country is successfully retrieved by code.
     *
     * Given: Repository returns a country for the specified code
     * When: Use case is invoked with a valid country code
     * Then: Use case should return ApiResponse.Success with the country
     */
    @Test
    fun `country is returned by repository successfully`() = runTest(testDispatcher) {
        // Given
        val countryCode = "USA"
        val expectedResponse = flowOf(ApiResponse.Success(sampleCountry))
        coEvery { countriesRepository.getCountryByCode(countryCode) } returns expectedResponse

        // When
        val result = useCase.invoke(countryCode).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue("Result should be Success", result[0] is ApiResponse.Success)
        
        val successResponse = result[0] as ApiResponse.Success
        assertEquals(sampleCountry, successResponse.data)
        assertEquals("United States", successResponse.data.name)
        assertEquals("USA", successResponse.data.threeLetterCode)
        
        // Verify repository was called with correct code
        verify { countriesRepository.getCountryByCode(countryCode) }
    }

    /**
     * Test: Repository returns error when country is not found.
     *
     * Given: Repository returns ApiResponse.Error for unknown country code
     * When: Use case is invoked with an unknown country code
     * Then: Use case should return ApiResponse.Error
     */
    @Test
    fun `repository returns error when country not found`() = runTest(testDispatcher) {
        // Given
        val countryCode = "XXX"
        val expectedException = Exception("Country with code 'XXX' not found")
        val expectedError = flowOf(ApiResponse.Error(expectedException))
        coEvery { countriesRepository.getCountryByCode(countryCode) } returns expectedError

        // When
        val result = useCase.invoke(countryCode).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue("Result should be Error", result[0] is ApiResponse.Error)
        
        val errorResponse = result[0] as ApiResponse.Error
        assertEquals(expectedException, errorResponse.exception)
        
        // Verify repository was called
        verify { countriesRepository.getCountryByCode(countryCode) }
    }

    /**
     * Test: Repository returns error when network exception occurs.
     *
     * Given: Repository throws IOException (network error)
     * When: Use case is invoked
     * Then: Use case should return ApiResponse.Error with the exception
     */
    @Test
    fun `repository returns error when network exception is thrown`() = runTest(testDispatcher) {
        // Given
        val countryCode = "USA"
        val expectedException = IOException("Network error")
        val expectedError = flowOf(ApiResponse.Error(expectedException))
        coEvery { countriesRepository.getCountryByCode(countryCode) } returns expectedError

        // When
        val result = useCase.invoke(countryCode).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue("Result should be Error", result[0] is ApiResponse.Error)
        
        val errorResponse = result[0] as ApiResponse.Error
        assertEquals(expectedException, errorResponse.exception)
        assertTrue(errorResponse.exception is IOException)
    }

    /**
     * Test: Use case normalizes country code to uppercase.
     *
     * Given: Repository is configured to accept uppercase codes
     * When: Use case is invoked with lowercase code
     * Then: Use case should normalize code to uppercase before calling repository
     */
    @Test
    fun `use case normalizes country code to uppercase`() = runTest(testDispatcher) {
        // Given
        val lowercaseCode = "usa"
        val normalizedCode = "USA"
        val expectedResponse = flowOf(ApiResponse.Success(sampleCountry))
        coEvery { countriesRepository.getCountryByCode(normalizedCode) } returns expectedResponse

        // When
        val result = useCase.invoke(lowercaseCode).toList()

        // Then
        assertTrue("Result should be Success", result[0] is ApiResponse.Success)
        
        // Verify repository was called with normalized (uppercase) code
        verify { countriesRepository.getCountryByCode(normalizedCode) }
    }

    /**
     * Test: Use case trims whitespace from country code.
     *
     * Given: Repository is configured to accept trimmed codes
     * When: Use case is invoked with code containing whitespace
     * Then: Use case should trim whitespace before calling repository
     */
    @Test
    fun `use case trims whitespace from country code`() = runTest(testDispatcher) {
        // Given
        val codeWithWhitespace = " USA "
        val trimmedCode = "USA"
        val expectedResponse = flowOf(ApiResponse.Success(sampleCountry))
        coEvery { countriesRepository.getCountryByCode(trimmedCode) } returns expectedResponse

        // When
        val result = useCase.invoke(codeWithWhitespace).toList()

        // Then
        assertTrue("Result should be Success", result[0] is ApiResponse.Success)
        
        // Verify repository was called with trimmed code
        verify { countriesRepository.getCountryByCode(trimmedCode) }
    }

    /**
     * Test: Use case throws exception for invalid country code length.
     *
     * Given: Country code is not exactly 3 characters
     * When: Use case is invoked with invalid code
     * Then: Use case should throw IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException::class)
    fun `use case throws exception for invalid code length`() = runTest(testDispatcher) {
        // Given
        val invalidCode = "US" // Only 2 characters

        // When
        useCase.invoke(invalidCode).toList()

        // Then: Should throw IllegalArgumentException
    }

    /**
     * Test: Use case throws exception for non-letter characters.
     *
     * Given: Country code contains non-letter characters
     * When: Use case is invoked with invalid code
     * Then: Use case should throw IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException::class)
    fun `use case throws exception for non-letter characters`() = runTest(testDispatcher) {
        // Given
        val invalidCode = "US1" // Contains number

        // When
        useCase.invoke(invalidCode).toList()

        // Then: Should throw IllegalArgumentException
    }

    /**
     * Test: Use case handles loading state correctly.
     *
     * Given: Repository emits Loading state
     * When: Use case is invoked
     * Then: Use case should propagate Loading state
     */
    @Test
    fun `use case propagates loading state from repository`() = runTest(testDispatcher) {
        // Given
        val countryCode = "USA"
        val expectedResponse = flowOf(
            ApiResponse.Loading,
            ApiResponse.Success(sampleCountry)
        )
        coEvery { countriesRepository.getCountryByCode(countryCode) } returns expectedResponse

        // When
        val result = useCase.invoke(countryCode).toList()

        // Then
        assertEquals(2, result.size)
        assertTrue("First result should be Loading", result[0] is ApiResponse.Loading)
        assertTrue("Second result should be Success", result[1] is ApiResponse.Success)
    }
}
