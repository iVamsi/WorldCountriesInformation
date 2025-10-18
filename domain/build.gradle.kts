/**
 * Domain module build configuration.
 *
 * This is a pure JVM/Kotlin module (not Android-specific) containing:
 * - Business logic and use cases
 * - Repository interfaces
 * - Domain models (via :core:model)
 *
 * Key principles:
 * - No Android dependencies (pure Kotlin)
 * - Platform-agnostic coroutines (kotlinx-coroutines-core)
 * - Test dependencies in test scope only
 * - API exposure for domain models and interfaces
 */
plugins {
    id("worldcountries.jvm.library")
}

dependencies {
    // Domain models - exposed to consumers
    api(project(":core:model"))

    // Dependency Injection annotations - exposed to consumers
    api(libs.javax.inject)

    // Coroutines - CORE (platform-agnostic, not Android-specific)
    // Use kotlinx-coroutines-core for pure JVM modules to avoid Android dependency
    api(libs.kotlinx.coroutines.core)

    // Testing dependencies - test scope only
    // tests-shared must be in testImplementation to keep it out of production classpath
    testImplementation(project(":tests-shared"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
