package com.example.facedetectionusingmlkit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.facedetectionusingmlkit.data.local.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.local.converters.FloatArrayTypeConverter
import com.example.facedetectionusingmlkit.data.local.converters.RectTypeConverter
import com.example.facedetectionusingmlkit.data.local.converters.SimilarFaceTypeConverter
import com.example.facedetectionusingmlkit.data.local.converters.UUIDTypeConverter
import com.example.facedetectionusingmlkit.data.local.converters.UriTypeConverter
import com.example.facedetectionusingmlkit.data.local.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotoFaceRefEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotosEntity

@Database(
    entities = [FacesEntity::class, PhotosEntity::class, PhotoFaceRefEntity::class, GalleryPhotoEntity::class],
    version = 1
)
@TypeConverters(
    UriTypeConverter::class,
    FloatArrayTypeConverter::class,
    UUIDTypeConverter::class,
    RectTypeConverter::class,
    SimilarFaceTypeConverter::class
)
abstract class FaceDetectionDatabase : RoomDatabase() {
    abstract fun facePhotoDao(): FacesAndPhotosDao
}