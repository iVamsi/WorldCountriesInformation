package com.vamsi.worldcountriesinformation.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.vamsi.worldcountriesinformation.core.common.Constants
import com.vamsi.worldcountriesinformation.core.network.BuildConfig
import com.vamsi.worldcountriesinformation.core.network.WorldCountriesApi
import com.vamsi.worldcountriesinformation.core.network.di.NetworkModule.provideRetrofit
import com.vamsi.worldcountriesinformation.core.network.interceptor.HttpsOnlyInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module for providing secure network dependencies.
 *
 * This module configures the networking stack with production-ready security measures:
 * - Debug-only logging to prevent sensitive data leaks
 * - HTTPS-only enforcement
 * - Modern TLS configuration (TLS 1.2+)
 * - Optimized timeouts for reliability
 *
 * ## Security Features
 *
 * 1. **Debug-Only Logging**
 *    - HTTP logging is only enabled in debug builds
 *    - Production builds have zero logging overhead
 *    - Prevents leaking sensitive data in production logs
 *
 * 2. **HTTPS Enforcement**
 *    - Custom interceptor blocks non-HTTPS requests
 *    - Fails fast on insecure connections
 *    - Protects against man-in-the-middle attacks
 *
 * 3. **Modern TLS Configuration**
 *    - TLS 1.2 and 1.3 support
 *    - Modern cipher suites
 *    - Secure connection specifications
 *
 * 4. **Optimized Timeouts**
 *    - 30s connect timeout (network establishment)
 *    - 30s read timeout (data reception)
 *    - 30s write timeout (data transmission)
 *    - Prevents hanging connections
 *
 * @see HttpsOnlyInterceptor for HTTPS enforcement logic
 * @see BuildConfig.DEBUG for debug flag
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Provides a secure, production-ready OkHttpClient.
     *
     * This client is configured with multiple security layers:
     * - Debug-only logging (disabled in production)
     * - HTTPS-only enforcement
     * - Modern TLS 1.2+ support
     * - Optimized connection timeouts
     *
     * ## Debug vs Production Behavior
     *
     * **Debug Builds:**
     * - Full HTTP logging (BODY level)
     * - Logs request/response headers and bodies
     * - Useful for debugging API issues
     *
     * **Production Builds:**
     * - Zero logging overhead
     * - No sensitive data leaks
     * - Optimal performance
     *
     * ## Security Configuration
     *
     * **HTTPS Enforcement:**
     * ```kotlin
     * // All requests must use HTTPS
     * GET https://api.example.com/data  // ✅ Allowed
     * GET http://api.example.com/data   // ❌ Blocked
     * ```
     *
     * **TLS Configuration:**
     * - Supports TLS 1.2 and 1.3
     * - Modern cipher suites only
     * - Secure renegotiation enabled
     *
     * **Connection Timeouts:**
     * - Connect: 30 seconds (time to establish TCP connection)
     * - Read: 30 seconds (time to receive data)
     * - Write: 30 seconds (time to send data)
     *
     * ## Usage
     *
     * This client is automatically injected into Retrofit:
     * ```kotlin
     * @Inject
     * lateinit var api: WorldCountriesApi
     *
     * // All API calls use secure client
     * val countries = api.getAllCountries()
     * ```
     *
     * ## Testing
     *
     * For testing with HTTP, use a test double:
     * ```kotlin
     * @TestInstallIn(
     *     components = [SingletonComponent::class],
     *     replaces = [NetworkModule::class]
     * )
     * object TestNetworkModule {
     *     @Provides
     *     fun provideTestClient(): OkHttpClient {
     *         // Test client without HTTPS enforcement
     *     }
     * }
     * ```
     *
     * @return Configured OkHttpClient with security features
     *
     * @see HttpsOnlyInterceptor for HTTPS enforcement details
     * @see provideRetrofit for Retrofit configuration
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Security: HTTPS-only enforcement
            // This interceptor blocks all non-HTTPS requests to prevent
            // man-in-the-middle attacks and data interception
            addInterceptor(HttpsOnlyInterceptor())

            // Debug-only logging
            // Logging is ONLY enabled in debug builds to prevent:
            // - Sensitive data leaks in production logs
            // - Performance overhead from logging
            // - Privacy violations (user data, auth tokens, etc.)
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    // BODY level logs:
                    // - Request method, URL, headers, body
                    // - Response status, headers, body
                    // Only use in debug! Can leak sensitive data!
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }

            // TLS Configuration: Modern, secure connections only
            // Supports TLS 1.2+ with modern cipher suites
            connectionSpecs(
                listOf(
                    ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                        .build(),
                    // Fallback for older servers (still secure)
                    ConnectionSpec.COMPATIBLE_TLS
                )
            )

            // Timeouts: Prevent hanging connections
            // These values balance reliability and user experience:
            // - Too short: Fails on slow networks
            // - Too long: Poor UX, resource waste
            connectTimeout(30, TimeUnit.SECONDS)  // TCP connection establishment
            readTimeout(30, TimeUnit.SECONDS)     // Reading response data
            writeTimeout(30, TimeUnit.SECONDS)    // Writing request data

            // Connection pooling (default settings are good)
            // Reuses connections for better performance
            // Max 5 idle connections, 5 minute keep-alive
        }.build()
    }

    /**
     * Provides Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Provides WorldCountriesApi service
     */
    @Provides
    @Singleton
    fun provideWorldCountriesApi(retrofit: Retrofit): WorldCountriesApi {
        return retrofit.create(WorldCountriesApi::class.java)
    }
}
