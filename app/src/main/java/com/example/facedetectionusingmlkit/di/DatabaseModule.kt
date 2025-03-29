package com.example.facedetectionusingmlkit.di

import android.content.Context
import androidx.room.Room
import com.example.facedetectionusingmlkit.data.FaceDetectionDatabase
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
}