package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.repository.KanaRepository
import com.example.myapplication.repository.PreferencesRepository
import com.example.myapplication.repository.WordRepository
import com.example.myapplication.service.TtsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKanaRepository(
        @ApplicationContext context: Context
    ): KanaRepository {
        return KanaRepository(context)
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        @ApplicationContext context: Context
    ): WordRepository {
        return WordRepository(context)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideTtsService(
        @ApplicationContext context: Context
    ): TtsService {
        return TtsService(context)
    }

    @Provides
    @Singleton
    fun provideSongRepository(
        @ApplicationContext context: Context
    ): com.example.myapplication.repository.SongRepository {
        return com.example.myapplication.repository.SongRepository(context)
    }

    @Provides
    @Singleton
    fun provideSentenceRepository(
        @ApplicationContext context: Context
    ): com.example.myapplication.repository.SentenceRepository {
        return com.example.myapplication.repository.SentenceRepository(context)
    }

    @Provides
    @Singleton
    fun provideDailyWordRepository(
        @ApplicationContext context: Context
    ): com.example.myapplication.repository.DailyWordRepository {
        return com.example.myapplication.repository.DailyWordRepository(context)
    }
}
