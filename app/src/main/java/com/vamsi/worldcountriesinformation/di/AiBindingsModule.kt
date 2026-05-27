package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.core.ai.AiCapabilityChecker
import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryGenerator
import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryGeneratorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiBindingsModule {

    @Binds
    @Singleton
    abstract fun bindCountrySummaryGenerator(
        impl: CountrySummaryGeneratorImpl,
    ): CountrySummaryGenerator
}

@Module
@InstallIn(SingletonComponent::class)
object AiProvidesModule {

    @Provides
    @Singleton
    fun provideAiCapabilityChecker(): AiCapabilityChecker = AiCapabilityChecker()
}
