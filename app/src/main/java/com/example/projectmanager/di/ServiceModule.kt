package com.example.projectmanager.di

import com.example.projectmanager.data.repository.ChatRepository
import com.example.projectmanager.data.repository.UserRepository
import com.example.projectmanager.service.ChatMessageListener
import com.example.projectmanager.service.ChatNotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideChatMessageListener(
        chatRepository: ChatRepository,
        userRepository: UserRepository,
        chatNotificationService: ChatNotificationService
    ): ChatMessageListener {
        return ChatMessageListener(chatRepository, userRepository, chatNotificationService)
    }
}
