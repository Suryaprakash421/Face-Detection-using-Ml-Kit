package com.example.facedetectionusingmlkit.di

import com.example.facedetectionusingmlkit.data.local.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMyRepository(facesAndPhotosDao: FacesAndPhotosDao): MyRepository {
        return MyRepository(facesAndPhotosDao)
    }
}