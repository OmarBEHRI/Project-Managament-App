package com.example.projectmanager.di

import com.example.projectmanager.navigation.AppNavigator
import com.example.projectmanager.navigation.AppNavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun bindAppNavigator(
        appNavigatorImpl: AppNavigatorImpl
    ): AppNavigator
} 