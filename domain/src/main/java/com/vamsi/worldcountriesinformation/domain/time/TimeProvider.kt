package com.vamsi.worldcountriesinformation.domain.time

/**
 * Wall-clock time in milliseconds since epoch. Injectable for tests (fixed millis) and production.
 */
fun interface TimeProvider {
    fun millis(): Long
}
