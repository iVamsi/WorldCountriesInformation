package com.vamsi.worldcountriesinformation.core.ai.di

import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryGenerator
import com.vamsi.worldcountriesinformation.core.ai.CountrySummaryGeneratorImpl
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
}
