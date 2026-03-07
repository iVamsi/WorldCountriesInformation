package com.vamsi.worldcountriesinformation.data.countries.search

import android.os.Build
import com.vamsi.worldcountriesinformation.domain.search.StructuredCountryQuery
import javax.inject.Inject

enum class PermissiveLicense {
    APACHE_2_0,
    MIT,
}

data class ApprovedOnDeviceModel(
    val modelId: String,
    val runtimeLicense: PermissiveLicense,
    val modelLicense: PermissiveLicense,
    val minimumMemoryMb: Long,
    val supportedAbis: Set<String>,
)

fun interface OnDeviceLlmRuntime {
    suspend fun interpret(query: String): StructuredCountryQuery?
}

fun interface OnDeviceLlmCountryQueryInterpreter {
    suspend fun interpretOrNull(query: String): StructuredCountryQuery?
}

class PermissiveModelCatalog @Inject constructor() {
    val preferredModel = ApprovedOnDeviceModel(
        modelId = "tinyllama-1.1b",
        runtimeLicense = PermissiveLicense.APACHE_2_0,
        modelLicense = PermissiveLicense.APACHE_2_0,
        minimumMemoryMb = 2048,
        supportedAbis = setOf("arm64-v8a"),
    )

    fun isApproved(model: ApprovedOnDeviceModel): Boolean {
        return model.runtimeLicense in setOf(PermissiveLicense.APACHE_2_0, PermissiveLicense.MIT) &&
            model.modelLicense in setOf(PermissiveLicense.APACHE_2_0, PermissiveLicense.MIT)
    }
}

class NaturalLanguageSearchCapabilityChecker @Inject constructor(
    private val permissiveModelCatalog: PermissiveModelCatalog,
) {

    fun canUseOnDeviceLlm(): Boolean {
        val model = permissiveModelCatalog.preferredModel
        val availableMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        val supportsAbi = Build.SUPPORTED_ABIS.any { abi -> model.supportedAbis.contains(abi) }

        return permissiveModelCatalog.isApproved(model) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            supportsAbi &&
            availableMemoryMb >= model.minimumMemoryMb
    }
}

class NoOpOnDeviceLlmRuntime @Inject constructor() : OnDeviceLlmRuntime {
    override suspend fun interpret(query: String): StructuredCountryQuery? = null
}

class CapabilityGatedOnDeviceLlmCountryQueryInterpreter @Inject constructor(
    private val capabilityChecker: NaturalLanguageSearchCapabilityChecker,
    private val runtime: OnDeviceLlmRuntime,
) : OnDeviceLlmCountryQueryInterpreter {

    override suspend fun interpretOrNull(query: String): StructuredCountryQuery? {
        if (!capabilityChecker.canUseOnDeviceLlm()) return null
        return runtime.interpret(query)
    }
}
