package com.example.facedetectionusingmlkit.di

import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.domain.usecase.GetDetectedFaceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetDetectedFaceUseCase(myRepository: MyRepository): GetDetectedFaceUseCase {
        return GetDetectedFaceUseCase(myRepository)
    }
}