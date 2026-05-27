package com.vamsi.worldcountriesinformation.core.ai

import android.os.Build
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks whether on-device generative AI can be used on this device.
 *
 * Requires Android 8.0+ and a successful probe of the GenerativeModel stack.
 * No network/API key is used; generation falls back at call time when Nano is absent.
 */
@Singleton
class AiCapabilityChecker @Inject constructor() {

    fun isOnDeviceGenerationAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }
        return runCatching {
            GenerativeModel(
                modelName = ON_DEVICE_MODEL_NAME,
                apiKey = LOCAL_ONLY_API_KEY,
                generationConfig = generationConfig {
                    maxOutputTokens = 256
                    temperature = 0.4f
                },
            )
            true
        }.getOrDefault(false)
    }

    companion object {
        const val ON_DEVICE_MODEL_NAME = "gemini-nano"
        internal const val LOCAL_ONLY_API_KEY = "local-only"
    }
}
