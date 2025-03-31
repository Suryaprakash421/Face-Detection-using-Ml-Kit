package com.example.facedetectionusingmlkit.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.facedetectionusingmlkit.data.FaceDetectionDatabase
import com.example.facedetectionusingmlkit.data.PrefManager
import com.example.facedetectionusingmlkit.data.dao.FacesAndPhotosDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFaceDetectionDatabase(@ApplicationContext context: Context): FaceDetectionDatabase {
        return Room.databaseBuilder(
            context,
            FaceDetectionDatabase::class.java,
            "face_database"
        ).build()
    }

    /**
     * Provide Face Photo Dao
     * */
    @Provides
    @Singleton
    fun provideFacesAndPhotosDao(faceDetectionDatabase: FaceDetectionDatabase): FacesAndPhotosDao {
        return faceDetectionDatabase.facePhotoDao()
    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("face_db", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePrefManager(sharedPreferences: SharedPreferences): PrefManager {
        return PrefManager(sharedPreferences)
    }
}