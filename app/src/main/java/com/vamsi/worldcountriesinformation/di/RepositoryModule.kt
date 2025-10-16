package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.data.countries.repository.CountriesRepositoryImpl
import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class Repository {

    @Binds
    @Singleton
    abstract fun bindCountriesRepository(impl: CountriesRepositoryImpl): CountriesRepository
}