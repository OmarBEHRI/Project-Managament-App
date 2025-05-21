package com.example.projectmanager.di

import com.example.projectmanager.data.service.GeminiAiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGeminiAiService(): GeminiAiService {
        return GeminiAiService()
    }
}
