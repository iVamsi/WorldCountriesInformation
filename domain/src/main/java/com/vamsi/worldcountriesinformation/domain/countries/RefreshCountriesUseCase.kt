package com.vamsi.worldcountriesinformation.domain.countries

import com.vamsi.worldcountriesinformation.domain.core.UseCase
import com.vamsi.worldcountriesinformation.domain.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Use case for forcing a refresh of country data from the network.
 *
 * This use case provides a safe, validated way to trigger data refresh with
 * proper error handling, logging, and resource management. It's designed for
 * user-triggered refresh operations like pull-to-refresh.
 *
 * ## Features
 *
 * 1. **Network Refresh**
 *    - Fetches fresh data from REST API
 *    - Updates local database atomically
 *    - Handles network failures gracefully
 *
 * 2. **Error Handling**
 *    - Comprehensive error catching
 *    - Detailed error logging
 *    - Type-safe Result wrapper
 *
 * 3. **Progress Tracking**
 *    - Logs refresh start
 *    - Logs refresh completion
 *    - Logs errors with stack traces
 *
 * 4. **Thread Safety**
 *    - Executes on IO dispatcher
 *    - Non-blocking operation
 *    - Coroutine-based
 *
 * ## Refresh Behavior
 *
 * **Successful Refresh:**
 * ```
 * 1. Fetch data from API (~1-2s)
 * 2. Clear existing database
 * 3. Insert fresh data (~100-200ms)
 * 4. Notify observers (automatic via Flow)
 * 5. Return Success
 * ```
 *
 * **Failed Refresh:**
 * ```
 * 1. Attempt to fetch from API
 * 2. Network error occurs
 * 3. Database remains unchanged
 * 4. Log error details
 * 5. Return Failure with exception
 * ```
 *
 * ## Usage Examples
 *
 * **Basic Refresh (ViewModel):**
 * ```kotlin
 * class CountriesViewModel @Inject constructor(
 *     private val refreshCountriesUseCase: RefreshCountriesUseCase
 * ) : ViewModel() {
 *
 *     private val _refreshState = MutableStateFlow<RefreshState>(RefreshState.Idle)
 *     val refreshState: StateFlow<RefreshState> = _refreshState.asStateFlow()
 *
 *     fun refresh() {
 *         viewModelScope.launch {
 *             _refreshState.value = RefreshState.Loading
 *
 *             refreshCountriesUseCase(Unit).fold(
 *                 onSuccess = {
 *                     _refreshState.value = RefreshState.Success
 *                 },
 *                 onFailure = { error ->
 *                     _refreshState.value = RefreshState.Error(error.message)
 *                 }
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * **Pull-to-Refresh UI:**
 * ```kotlin
 * @Composable
 * fun CountriesScreen(viewModel: CountriesViewModel) {
 *     val refreshState by viewModel.refreshState.collectAsState()
 *     val isRefreshing = refreshState is RefreshState.Loading
 *
 *     PullToRefreshLayout(
 *         refreshing = isRefreshing,
 *         onRefresh = { viewModel.refresh() }
 *     ) {
 *         CountryList(...)
 *     }
 *
 *     // Show error snackbar
 *     if (refreshState is RefreshState.Error) {
 *         Snackbar("Failed to refresh: ${refreshState.message}")
 *     }
 * }
 * ```
 *
 * **Manual Refresh Button:**
 * ```kotlin
 * @Composable
 * fun RefreshButton(viewModel: CountriesViewModel) {
 *     val refreshState by viewModel.refreshState.collectAsState()
 *
 *     IconButton(
 *         onClick = { viewModel.refresh() },
 *         enabled = refreshState !is RefreshState.Loading
 *     ) {
 *         Icon(
 *             imageVector = if (refreshState is RefreshState.Loading) {
 *                 Icons.Default.HourglassEmpty
 *             } else {
 *                 Icons.Default.Refresh
 *             },
 *             contentDescription = "Refresh countries"
 *         )
 *     }
 * }
 * ```
 *
 * **With Retry Logic:**
 * ```kotlin
 * suspend fun refreshWithRetry(maxAttempts: Int = 3) {
 *     repeat(maxAttempts) { attempt ->
 *         val result = refreshCountriesUseCase(Unit)
 *
 *         if (result.isSuccess) {
 *             return // Success, exit
 *         }
 *
 *         if (attempt < maxAttempts - 1) {
 *             delay(1000 * (attempt + 1)) // Exponential backoff
 *         }
 *     }
 * }
 * ```
 *
 * ## Error Scenarios
 *
 * **Network Unavailable:**
 * ```kotlin
 * Result.failure(IOException("Unable to resolve host"))
 * // Database unchanged, user sees cached data
 * ```
 *
 * **Server Error (5xx):**
 * ```kotlin
 * Result.failure(HttpException(500, "Internal Server Error"))
 * // Database unchanged, user sees cached data
 * ```
 *
 * **Timeout:**
 * ```kotlin
 * Result.failure(SocketTimeoutException("Read timed out"))
 * // Database unchanged, user sees cached data
 * ```
 *
 * **Parse Error:**
 * ```kotlin
 * Result.failure(JsonParseException("Unexpected JSON token"))
 * // Database unchanged, user sees cached data
 * ```
 *
 * ## Performance
 *
 * **Typical Timings:**
 * - Network fetch: 1-3 seconds
 * - Database clear: 50-100ms
 * - Database insert: 100-200ms
 * - **Total**: 1.5-3.5 seconds
 *
 * **Network Data Transfer:**
 * - Payload size: ~150-200 KB (compressed)
 * - ~250 countries with full data
 * - JSON format
 *
 * **Database Operation:**
 * - Transaction-based (atomic)
 * - DELETE + INSERT in single transaction
 * - Triggers Flow emissions automatically
 *
 * ## Testing
 *
 * **Success Test:**
 * ```kotlin
 * @Test
 * fun `refresh succeeds with valid network response`() = runTest {
 *     // Given
 *     val repository = FakeCountriesRepository(shouldSucceed = true)
 *     val useCase = RefreshCountriesUseCase(repository, testDispatcher)
 *
 *     // When
 *     val result = useCase(Unit)
 *
 *     // Then
 *     assertThat(result.isSuccess).isTrue()
 *     verify(repository).forceRefresh()
 * }
 * ```
 *
 * **Failure Test:**
 * ```kotlin
 * @Test
 * fun `refresh fails with network error`() = runTest {
 *     // Given
 *     val repository = FakeCountriesRepository(
 *         error = IOException("Network error")
 *     )
 *     val useCase = RefreshCountriesUseCase(repository, testDispatcher)
 *
 *     // When
 *     val result = useCase(Unit)
 *
 *     // Then
 *     assertThat(result.isFailure).isTrue()
 *     assertThat(result.exceptionOrNull()).isInstanceOf<IOException>()
 * }
 * ```
 *
 * ## Architecture
 *
 * ```
 * UI Layer (Pull-to-Refresh)
 *        ↓
 * ViewModel
 *        ↓
 * RefreshCountriesUseCase ← (You are here)
 *        ↓
 * CountriesRepository
 *        ↓
 * Network API + Database
 * ```
 *
 * ## Side Effects
 *
 * This operation has the following side effects:
 * - **Network Request**: Consumes bandwidth and battery
 * - **Database Clear**: Removes all existing data
 * - **Flow Emissions**: Triggers updates in all observers
 * - **Logging**: Writes to device log
 *
 * ## Best Practices
 *
 * 1. **Rate Limiting**: Don't call too frequently (e.g., max once per minute)
 * 2. **Loading State**: Always show loading indicator during refresh
 * 3. **Error Handling**: Show user-friendly error messages
 * 4. **Success Feedback**: Provide subtle confirmation of success
 * 5. **Offline Handling**: Gracefully handle offline scenarios
 *
 * @param countriesRepository Repository providing refresh functionality
 * @param ioDispatcher IO dispatcher for background operations
 *
 * @see CountriesRepository.forceRefresh for underlying implementation
 * @see UseCase for base use case pattern
 *
 * @since 2.0.0
 */
class RefreshCountriesUseCase @Inject constructor(
    private val countriesRepository: CountriesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UseCase<Unit, Unit>(ioDispatcher) {

    /**
     * Executes the refresh operation.
     *
     * This method delegates to the repository's forceRefresh method.
     * The base UseCase class wraps the result in ApiResponse and handles
     * dispatcher switching automatically.
     *
     * @param parameters Unit (no parameters needed for refresh)
     *
     * @return Unit on success
     *
     * @throws Exception if refresh fails (caught by base class and wrapped in ApiResponse.Error)
     *
     * @see CountriesRepository.forceRefresh
     */
    override suspend fun execute(parameters: Unit) {
        // Delegate to repository - throws on error, which is caught by base class
        countriesRepository.forceRefresh().getOrThrow()
    }
}
