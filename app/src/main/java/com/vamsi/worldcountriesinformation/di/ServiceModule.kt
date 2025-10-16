package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.core.constants.Constants
import com.vamsi.worldcountriesinformation.data.remote.WorldCountriesInformationAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ServiceModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.TEST_BASE_URL)
            .addConverterFactory(
                MoshiConverterFactory.create()
            )
            .build()
    }

    @Singleton
    @Provides
    fun provideWorldCountriesInformationApi(retrofit: Retrofit): WorldCountriesInformationAPI {
        return retrofit.create(WorldCountriesInformationAPI::class.java)
    }
}