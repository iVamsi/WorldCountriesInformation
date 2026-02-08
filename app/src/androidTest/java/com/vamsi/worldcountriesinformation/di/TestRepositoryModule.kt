package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.domain.countries.CountriesRepository
import com.vamsi.worldcountriesinformation.fake.FakeCountriesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Test module that replaces the production [Repository] module.
 *
 * This module provides a [FakeCountriesRepository] instead of the real implementation,
 * allowing UI tests to control the data and behavior of the repository.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [Repository::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideFakeCountriesRepository(): FakeCountriesRepository {
        return FakeCountriesRepository()
    }

    @Provides
    @Singleton
    fun provideCountriesRepository(
        fakeRepository: FakeCountriesRepository
    ): CountriesRepository {
        return fakeRepository
    }
}
