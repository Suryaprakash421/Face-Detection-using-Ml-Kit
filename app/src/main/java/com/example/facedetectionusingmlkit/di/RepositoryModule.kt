package com.example.facedetectionusingmlkit.di

import android.content.Context
import com.example.facedetectionusingmlkit.data.local.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideMyRepository(@ApplicationContext context: Context, facesAndPhotosDao: FacesAndPhotosDao): MyRepository {
        return MyRepository(context, facesAndPhotosDao)
    }
}