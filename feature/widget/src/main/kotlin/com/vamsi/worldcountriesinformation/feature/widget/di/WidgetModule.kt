package com.vamsi.worldcountriesinformation.feature.widget.di

import com.vamsi.worldcountriesinformation.feature.widget.liveupdate.LiveUpdateManager
import com.vamsi.worldcountriesinformation.feature.widget.liveupdate.LiveUpdateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindLiveUpdateManager(impl: LiveUpdateManagerImpl): LiveUpdateManager
}
