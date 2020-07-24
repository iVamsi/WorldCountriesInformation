package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.data.repository.CountriesRepositoryImpl
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
abstract class Repository {

    @Binds
    abstract fun bindCountriesRepository(impl: CountriesRepositoryImpl): CountriesRepository
}