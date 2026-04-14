package com.vamsi.worldcountriesinformation.core.datastore.di

import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesDataSource
import com.vamsi.worldcountriesinformation.core.datastore.SearchPreferencesPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreBindsModule {

    @Binds
    @Singleton
    abstract fun bindSearchPreferencesPort(
        impl: SearchPreferencesDataSource,
    ): SearchPreferencesPort
}
