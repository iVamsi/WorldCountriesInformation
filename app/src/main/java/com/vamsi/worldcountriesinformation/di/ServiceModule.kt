package com.vamsi.worldcountriesinformation.di

import com.vamsi.worldcountriesinformation.core.constants.Constants.BASE_URL
import com.vamsi.worldcountriesinformation.core.constants.Constants.TEST_BASE_URL
import com.vamsi.worldcountriesinformation.data.remote.WorldCountriesInformationAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object ServiceModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TEST_BASE_URL)
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