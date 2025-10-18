package com.vamsi.worldcountriesinformation.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor that enforces HTTPS-only connections.
 *
 * This interceptor provides a critical security layer by blocking all non-HTTPS
 * requests before they reach the network. It prevents accidental insecure
 * connections that could expose sensitive data to man-in-the-middle attacks.
 *
 * ## Security Benefits
 *
 * 1. **Data Encryption**
 *    - All data transmitted over HTTPS is encrypted
 *    - Prevents eavesdropping and data interception
 *    - Protects user privacy and sensitive information
 *
 * 2. **Man-in-the-Middle Prevention**
 *    - HTTPS verifies server identity via certificates
 *    - Prevents attackers from impersonating servers
 *    - Ensures data integrity (no tampering)
 *
 * 3. **Fail-Fast Behavior**
 *    - Blocks insecure requests immediately
 *    - No network round-trip for HTTP requests
 *    - Clear error messages for debugging
 *
 * 4. **Compliance**
 *    - Helps meet security requirements (GDPR, HIPAA, etc.)
 *    - App Store guidelines often require HTTPS
 *    - Industry best practice
 *
 * ## How It Works
 *
 * ```
 * Request Flow:
 * 
 * API Call → Interceptor Chain → HttpsOnlyInterceptor → Network
 *                                          ↓
 *                                    Check Scheme
 *                                          ↓
 *                        ┌─────────────────┴─────────────────┐
 *                        ↓                                   ↓
 *                   HTTPS (https://)                    HTTP (http://)
 *                        ↓                                   ↓
 *                  Allow Request                      Throw Exception
 *                        ↓                                   ↓
 *                  Continue Chain                    Request Fails
 * ```
 *
 * ## Usage
 *
 * Add this interceptor to your OkHttpClient:
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(HttpsOnlyInterceptor())
 *     .build()
 * ```
 *
 * ## Examples
 *
 * **Allowed Requests:**
 * ```kotlin
 * GET https://restcountries.com/v3.1/all         // ✅ Allowed
 * GET https://ivamsi.github.io/api/all/          // ✅ Allowed
 * GET https://localhost:8080/test                // ✅ Allowed (HTTPS)
 * ```
 *
 * **Blocked Requests:**
 * ```kotlin
 * GET http://restcountries.com/v3.1/all          // ❌ Blocked
 * GET http://example.com/api/data                // ❌ Blocked
 * GET http://localhost:8080/test                 // ❌ Blocked
 * 
 * // All throw: InsecureConnectionException
 * ```
 *
 * ## Error Handling
 *
 * When a non-HTTPS request is attempted:
 * ```kotlin
 * try {
 *     val response = api.getData() // http:// URL
 * } catch (e: IOException) {
 *     // Message: "Insecure HTTP connection blocked: http://example.com"
 *     // Fix: Update URL to use https://
 * }
 * ```
 *
 * ## Testing
 *
 * For local development or testing:
 *
 * **Option 1: Test Double**
 * ```kotlin
 * // Replace the entire NetworkModule in tests
 * @TestInstallIn(
 *     components = [SingletonComponent::class],
 *     replaces = [NetworkModule::class]
 * )
 * object TestNetworkModule {
 *     @Provides
 *     fun provideTestClient() = OkHttpClient.Builder()
 *         // No HttpsOnlyInterceptor for tests
 *         .build()
 * }
 * ```
 *
 * **Option 2: Use HTTPS Locally**
 * ```bash
 * # Set up local HTTPS server
 * openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365
 * python3 -m http.server 8080 --key key.pem --cert cert.pem
 * ```
 *
 * ## Performance
 *
 * - **Zero overhead for HTTPS requests**
 * - Simple string comparison (scheme check)
 * - No network calls or blocking operations
 * - Negligible CPU/memory impact
 *
 * ## Thread Safety
 *
 * This interceptor is **thread-safe** and **stateless**:
 * - No mutable state
 * - No shared resources
 * - Safe for concurrent use by multiple threads
 * - OkHttp handles thread safety of the chain
 *
 * @see Interceptor for OkHttp interceptor documentation
 * @see Interceptor.Chain for request processing chain
 * @throws IOException when a non-HTTPS request is attempted
 */
class HttpsOnlyInterceptor : Interceptor {

    /**
     * Intercepts outgoing requests and enforces HTTPS-only policy.
     *
     * This method is called for every network request. It checks if the
     * request URL uses HTTPS scheme. If not, it throws an exception to
     * prevent the insecure connection.
     *
     * ## Algorithm
     *
     * 1. Extract URL from request
     * 2. Check URL scheme
     * 3. If HTTPS → Allow (proceed with chain)
     * 4. If HTTP → Block (throw exception)
     *
     * ## Performance
     *
     * - O(1) time complexity
     * - No network calls
     * - No blocking operations
     * - Happens before network request
     *
     * @param chain The interceptor chain for processing the request
     * @return The response from the server (for HTTPS requests only)
     * @throws IOException if the request uses HTTP (non-secure) protocol
     *
     * @see Interceptor.Chain.request for accessing the request
     * @see Interceptor.Chain.proceed for continuing the chain
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url

        // Security check: Ensure HTTPS
        if (url.scheme != "https") {
            throw IOException(
                "Insecure HTTP connection blocked. " +
                "Only HTTPS connections are allowed for security. " +
                "Attempted URL: $url"
            )
        }

        // HTTPS verified, proceed with request
        return chain.proceed(request)
    }

    /**
     * Custom exception for insecure connection attempts.
     *
     * This is a specialized IOException that provides clear context
     * about why the connection was blocked.
     *
     * @param url The insecure URL that was blocked
     */
    class InsecureConnectionException(url: String) : IOException(
        "Insecure HTTP connection blocked: $url. " +
        "Only HTTPS connections are allowed. " +
        "Update your URL to use https:// instead of http://"
    )
}
