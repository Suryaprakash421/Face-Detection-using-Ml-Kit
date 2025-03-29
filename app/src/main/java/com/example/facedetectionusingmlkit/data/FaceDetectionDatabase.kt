package com.example.facedetectionusingmlkit.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.facedetectionusingmlkit.data.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.converters.FloatArrayTypeConverter
import com.example.facedetectionusingmlkit.data.converters.RectTypeConverter
import com.example.facedetectionusingmlkit.data.converters.UUIDTypeConverter
import com.example.facedetectionusingmlkit.data.converters.UriTypeConverter
import com.example.facedetectionusingmlkit.data.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.entity.PhotoFaceRefEntity
import com.example.facedetectionusingmlkit.data.entity.PhotosEntity

@Database(
    entities = [FacesEntity::class, PhotosEntity::class, PhotoFaceRefEntity::class],
    version = 1
)
@TypeConverters(
    UriTypeConverter::class,
    FloatArrayTypeConverter::class,
    UUIDTypeConverter::class,
    RectTypeConverter::class
)
abstract class FaceDetectionDatabase : RoomDatabase() {
    abstract fun facePhotoDao(): FacesAndPhotosDao
}