package com.vamsi.worldcountriesinformation.feature.widget.di

import com.vamsi.worldcountriesinformation.domain.countries.GetCountriesUseCase
import com.vamsi.worldcountriesinformation.feature.widget.data.WidgetDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for widget dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object WidgetModule {

    @Provides
    @Singleton
    fun provideWidgetDataSource(
        getCountriesUseCase: GetCountriesUseCase,
    ): WidgetDataSource {
        return WidgetDataSource(getCountriesUseCase)
    }
}

