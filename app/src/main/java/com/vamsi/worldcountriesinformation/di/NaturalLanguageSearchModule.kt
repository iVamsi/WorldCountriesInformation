package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.data.countries.search.CapabilityGatedOnDeviceLlmCountryQueryInterpreter
import com.vamsi.worldcountriesinformation.data.countries.search.FallbackCountryQueryInterpreter
import com.vamsi.worldcountriesinformation.data.countries.search.NoOpOnDeviceLlmRuntime
import com.vamsi.worldcountriesinformation.data.countries.search.OnDeviceLlmCountryQueryInterpreter
import com.vamsi.worldcountriesinformation.data.countries.search.OnDeviceLlmRuntime
import com.vamsi.worldcountriesinformation.domain.search.CountryQueryInterpreter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NaturalLanguageSearchModule {

    @Provides
    @Singleton
    fun provideOnDeviceLlmRuntime(): OnDeviceLlmRuntime = NoOpOnDeviceLlmRuntime()

    @Provides
    @Singleton
    fun provideOnDeviceLlmCountryQueryInterpreter(
        capabilityGatedInterpreter: CapabilityGatedOnDeviceLlmCountryQueryInterpreter,
    ): OnDeviceLlmCountryQueryInterpreter = capabilityGatedInterpreter

    @Provides
    @Singleton
    fun provideCountryQueryInterpreter(
        fallbackCountryQueryInterpreter: FallbackCountryQueryInterpreter,
    ): CountryQueryInterpreter = fallbackCountryQueryInterpreter
}
