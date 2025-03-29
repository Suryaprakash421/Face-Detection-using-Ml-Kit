package com.example.facedetectionusingmlkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.facedetectionusingmlkit.data.entity.GalleryPhotoEntity

@Dao
interface FacesAndPhotosDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGalleryPhotos(galleryPhotoEntity: List<GalleryPhotoEntity>)
}