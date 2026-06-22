package com.vamsi.worldcountriesinformation.core.ai.di

import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryGenerator
import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryGeneratorImpl
import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryPortAdapter
import com.vamsi.worldcountriesinformation.domain.countries.CountrySummaryPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindCountrySummaryGenerator(
        impl: CountrySummaryGeneratorImpl,
    ): CountrySummaryGenerator

    @Binds
    @Singleton
    abstract fun bindCountrySummaryPort(
        impl: CountrySummaryPortAdapter,
    ): CountrySummaryPort
}
