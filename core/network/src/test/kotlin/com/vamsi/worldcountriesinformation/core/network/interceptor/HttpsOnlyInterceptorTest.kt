package com.vamsi.worldcountriesinformation.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for [HttpsOnlyInterceptor].
 *
 * These tests verify that the interceptor correctly enforces HTTPS-only
 * connections by blocking HTTP requests and allowing HTTPS requests.
 *
 * ## Test Strategy
 *
 * 1. **Positive Tests**: Verify HTTPS requests are allowed
 * 2. **Negative Tests**: Verify HTTP requests are blocked
 * 3. **Edge Cases**: Test various URL formats and scenarios
 * 4. **Error Messages**: Verify clear, helpful error messages
 *
 * ## Test Coverage
 *
 * - ✅ HTTPS requests (various formats)
 * - ✅ HTTP requests (should be blocked)
 * - ✅ Different ports
 * - ✅ Localhost connections
 * - ✅ Subdomains
 * - ✅ Error message clarity
 *
 * @see HttpsOnlyInterceptor for the interceptor implementation
 */
class HttpsOnlyInterceptorTest {

    private val interceptor = HttpsOnlyInterceptor()

    /**
     * Test: HTTPS request is allowed to proceed.
     *
     * Given: A request to a valid HTTPS URL
     * When: The interceptor processes the request
     * Then: The request should proceed without exceptions
     *
     * This verifies the interceptor doesn't block legitimate HTTPS traffic.
     */
    @Test
    fun `https request is allowed`() {
        // Given
        val request = createRequest("https://restcountries.com/v3.1/all")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
        assertEquals("https://restcountries.com/v3.1/all", response.request.url.toString())
    }

    /**
     * Test: HTTP request is blocked with clear error.
     *
     * Given: A request to an HTTP (insecure) URL
     * When: The interceptor processes the request
     * Then: An IOException should be thrown with a descriptive message
     *
     * This is the core security feature - blocking insecure connections.
     */
    @Test
    fun `http request is blocked`() {
        // Given
        val request = createRequest("http://restcountries.com/v3.1/all")
        val chain = createMockChain(request)

        // When & Then
        val exception = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        // Verify error message is clear and helpful
        assertTrue(
            "Error message should mention the URL",
            exception.message?.contains("http://restcountries.com/v3.1/all") == true
        )
        assertTrue(
            "Error message should mention HTTPS",
            exception.message?.contains("HTTPS") == true
        )
        assertTrue(
            "Error message should mention insecure/blocked",
            exception.message?.contains("blocked", ignoreCase = true) == true
        )
    }

    /**
     * Test: HTTPS request with custom port is allowed.
     *
     * Given: A request to HTTPS URL with non-standard port (8443)
     * When: The interceptor processes the request
     * Then: The request should proceed (port doesn't matter, only scheme)
     *
     * Verifies that custom HTTPS ports are allowed.
     */
    @Test
    fun `https request with custom port is allowed`() {
        // Given
        val request = createRequest("https://api.example.com:8443/data")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
    }

    /**
     * Test: HTTP request with custom port is blocked.
     *
     * Given: A request to HTTP URL with custom port (8080)
     * When: The interceptor processes the request
     * Then: Should be blocked (HTTP is insecure regardless of port)
     *
     * Verifies that custom ports don't bypass the HTTP block.
     */
    @Test
    fun `http request with custom port is blocked`() {
        // Given
        val request = createRequest("http://localhost:8080/api/test")
        val chain = createMockChain(request)

        // When & Then
        assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }
    }

    /**
     * Test: HTTPS localhost is allowed.
     *
     * Given: A request to HTTPS localhost
     * When: The interceptor processes the request
     * Then: Should be allowed (HTTPS localhost is secure)
     *
     * Important for development and testing scenarios.
     */
    @Test
    fun `https localhost is allowed`() {
        // Given
        val request = createRequest("https://localhost:8443/api/test")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
    }

    /**
     * Test: HTTP localhost is blocked.
     *
     * Given: A request to HTTP localhost
     * When: The interceptor processes the request
     * Then: Should be blocked (even localhost must use HTTPS)
     *
     * Enforces security even for local development.
     * For testing, use network security config debug-overrides.
     */
    @Test
    fun `http localhost is blocked`() {
        // Given
        val request = createRequest("http://localhost:8080/api/test")
        val chain = createMockChain(request)

        // When & Then
        assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }
    }

    /**
     * Test: HTTPS with IP address is allowed.
     *
     * Given: A request to HTTPS with IP address (Android emulator host)
     * When: The interceptor processes the request
     * Then: Should be allowed (HTTPS with IP is valid)
     */
    @Test
    fun `https with ip address is allowed`() {
        // Given
        val request = createRequest("https://10.0.2.2:8443/api")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
    }

    /**
     * Test: HTTP with IP address is blocked.
     *
     * Given: A request to HTTP with IP address
     * When: The interceptor processes the request
     * Then: Should be blocked (HTTP is insecure with IP too)
     */
    @Test
    fun `http with ip address is blocked`() {
        // Given
        val request = createRequest("http://192.168.1.100/api")
        val chain = createMockChain(request)

        // When & Then
        assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }
    }

    /**
     * Test: HTTPS with subdomain is allowed.
     *
     * Given: A request to HTTPS URL with subdomain
     * When: The interceptor processes the request
     * Then: Should be allowed (subdomains are fine)
     */
    @Test
    fun `https with subdomain is allowed`() {
        // Given
        val request = createRequest("https://api.staging.example.com/v1/data")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
    }

    /**
     * Test: HTTP with subdomain is blocked.
     *
     * Given: A request to HTTP URL with subdomain
     * When: The interceptor processes the request
     * Then: Should be blocked
     */
    @Test
    fun `http with subdomain is blocked`() {
        // Given
        val request = createRequest("http://api.staging.example.com/v1/data")
        val chain = createMockChain(request)

        // When & Then
        assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }
    }

    /**
     * Test: HTTPS with query parameters is allowed.
     *
     * Given: A request to HTTPS URL with query parameters
     * When: The interceptor processes the request
     * Then: Should be allowed (query params don't affect scheme)
     */
    @Test
    fun `https with query parameters is allowed`() {
        // Given
        val request = createRequest("https://api.example.com/search?q=test&limit=10")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
    }

    /**
     * Test: HTTP with query parameters is blocked.
     *
     * Given: A request to HTTP URL with query parameters
     * When: The interceptor processes the request
     * Then: Should be blocked
     */
    @Test
    fun `http with query parameters is blocked`() {
        // Given
        val request = createRequest("http://api.example.com/search?q=test&limit=10")
        val chain = createMockChain(request)

        // When & Then
        assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }
    }

    /**
     * Test: HTTPS GitHub Pages URL is allowed.
     *
     * Given: A request to the actual fallback API (GitHub Pages)
     * When: The interceptor processes the request
     * Then: Should be allowed
     *
     * This tests a real-world URL used by the app.
     */
    @Test
    fun `https github pages url is allowed`() {
        // Given
        val request = createRequest("https://ivamsi.github.io/WorldCountriesAPI/api/all/")
        val chain = createMockChain(request)

        // When
        val response = interceptor.intercept(chain)

        // Then
        assertEquals(200, response.code)
    }

    /**
     * Test: Error message includes the blocked URL.
     *
     * Given: An HTTP request
     * When: The interceptor blocks it
     * Then: The error message should include the exact URL
     *
     * This helps developers quickly identify and fix the issue.
     */
    @Test
    fun `error message includes blocked url`() {
        // Given
        val url = "http://insecure.example.com/api/data"
        val request = createRequest(url)
        val chain = createMockChain(request)

        // When
        val exception = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        // Then
        assertTrue(
            "Error message should contain the blocked URL",
            exception.message?.contains(url) == true
        )
    }

    /**
     * Test: Error message suggests using HTTPS.
     *
     * Given: An HTTP request
     * When: The interceptor blocks it
     * Then: Error message should suggest using HTTPS
     *
     * This makes the error actionable for developers.
     */
    @Test
    fun `error message suggests using https`() {
        // Given
        val request = createRequest("http://example.com")
        val chain = createMockChain(request)

        // When
        val exception = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        // Then
        val message = exception.message?.lowercase() ?: ""
        assertTrue(
            "Error message should mention 'https'",
            message.contains("https")
        )
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Creates a test request with the given URL.
     *
     * @param url The URL string for the request
     * @return A Request object for testing
     */
    private fun createRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    /**
     * Creates a mock interceptor chain for testing.
     *
     * This chain returns a successful response for any request that
     * reaches it (i.e., wasn't blocked by the interceptor).
     *
     * @param request The request to process
     * @return A mock Interceptor.Chain implementation
     */
    private fun createMockChain(request: Request): Interceptor.Chain {
        return object : Interceptor.Chain {
            override fun request(): Request = request

            override fun proceed(request: Request): Response {
                // Return a mock successful response
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_2)
                    .code(200)
                    .message("OK")
                    .build()
            }

            // These methods are not used in tests
            override fun connection() = null
            override fun call() = throw NotImplementedError()
            override fun connectTimeoutMillis() = 30000
            override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun readTimeoutMillis() = 30000
            override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
            override fun writeTimeoutMillis() = 30000
            override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        }
    }
}
