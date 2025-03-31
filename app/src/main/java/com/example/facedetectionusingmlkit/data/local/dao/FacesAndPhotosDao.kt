package com.example.facedetectionusingmlkit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacesAndPhotosDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGalleryPhotos(galleryPhotoEntity: List<GalleryPhotoEntity>)

    @Query("SELECT * FROM gallery_photo")
    fun getGalleryImageAsFlow(): Flow<List<GalleryPhotoEntity>>
}