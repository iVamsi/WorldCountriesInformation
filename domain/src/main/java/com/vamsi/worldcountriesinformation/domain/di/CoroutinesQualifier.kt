/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vamsi.worldcountriesinformation.domain.di

import javax.inject.Qualifier

/**
 * Coroutine dispatcher and scope qualifiers for dependency injection.
 *
 * These qualifiers are platform-agnostic and can be used in pure JVM/Kotlin modules.
 * The actual dispatcher implementations (Dispatchers.Default, Dispatchers.IO, etc.)
 * are provided by platform-specific modules (e.g., :app module for Android).
 *
 * **Architecture Benefits:**
 * - Domain layer remains platform-agnostic
 * - Enables testing with test dispatchers
 * - Supports different implementations per platform
 * - Clear separation of concerns
 *
 * @see kotlinx.coroutines.CoroutineDispatcher
 * @since 1.0.0
 */

/**
 * Qualifier for the Default dispatcher.
 *
 * **Use for:**
 * - CPU-intensive work
 * - Complex computations
 * - Data processing
 * - Sorting and filtering large datasets
 *
 * **Default Implementation:**
 * On Android: `Dispatchers.Default`
 * - Shared thread pool sized to CPU cores
 * - Optimized for CPU-bound operations
 *
 * Example:
 * ```kotlin
 * class DataProcessor @Inject constructor(
 *     @DefaultDispatcher private val dispatcher: CoroutineDispatcher
 * )
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

/**
 * Qualifier for the IO dispatcher.
 *
 * **Use for:**
 * - Network operations
 * - Database queries
 * - File I/O
 * - Disk operations
 * - Any blocking I/O work
 *
 * **Default Implementation:**
 * On Android: `Dispatchers.IO`
 * - Large thread pool (up to 64 threads)
 * - Designed for blocking I/O operations
 * - Automatically manages thread lifecycle
 *
 * **Best Practice:**
 * Most use cases and repositories should use this dispatcher
 * as they typically interact with network/database.
 *
 * Example:
 * ```kotlin
 * class GetCountriesUseCase @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * ) : FlowUseCase<Boolean, List<Country>>(ioDispatcher)
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

/**
 * Qualifier for the Main dispatcher.
 *
 * **Use for:**
 * - UI updates
 * - LiveData/StateFlow emissions
 * - UI-related coroutines
 * - Short, non-blocking operations
 *
 * **Default Implementation:**
 * On Android: `Dispatchers.Main`
 * - Runs on the main/UI thread
 * - Should only be used for UI updates
 * - Avoid long-running work on this dispatcher
 *
 * **Warning:**
 * Blocking this dispatcher will freeze the UI!
 *
 * Example:
 * ```kotlin
 * viewModelScope.launch(mainDispatcher) {
 *     _uiState.value = UiState.Success(data)
 * }
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher

/**
 * Qualifier for the Main.immediate dispatcher.
 *
 * **Use for:**
 * - Immediate UI updates (no dispatch delay)
 * - Time-sensitive UI operations
 * - Operations already on main thread
 *
 * **Default Implementation:**
 * On Android: `Dispatchers.Main.immediate`
 * - Executes immediately if already on main thread
 * - Avoids unnecessary dispatch overhead
 * - Faster than regular Main dispatcher
 *
 * **Difference from MainDispatcher:**
 * - MainDispatcher: Always posts to message queue
 * - MainImmediateDispatcher: Executes immediately if possible
 *
 * Example:
 * ```kotlin
 * withContext(mainImmediateDispatcher) {
 *     // Update UI immediately if already on main thread
 *     updateView()
 * }
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher

/**
 * Qualifier for the application-wide CoroutineScope.
 *
 * **Use for:**
 * - Long-running application-level operations
 * - Background sync tasks
 * - Cache cleanup
 * - Operations that outlive ViewModels
 *
 * **Lifecycle:**
 * - Lives as long as the application
 * - Survives Activity/Fragment destruction
 * - Cancelled only when app process is killed
 *
 * **Default Implementation:**
 * Typically provided in Application class:
 * ```kotlin
 * @ApplicationScope
 * @Provides
 * @Singleton
 * fun provideApplicationScope(): CoroutineScope {
 *     return CoroutineScope(SupervisorJob() + Dispatchers.Default)
 * }
 * ```
 *
 * Example:
 * ```kotlin
 * class BackgroundSyncManager @Inject constructor(
 *     @ApplicationScope private val scope: CoroutineScope
 * ) {
 *     fun startPeriodicSync() {
 *         scope.launch {
 *             // Runs throughout app lifetime
 *         }
 *     }
 * }
 * ```
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope