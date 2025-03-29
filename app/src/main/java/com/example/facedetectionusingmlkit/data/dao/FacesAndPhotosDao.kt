package com.example.facedetectionusingmlkit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.facedetectionusingmlkit.data.entity.GalleryPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FacesAndPhotosDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGalleryPhotos(galleryPhotoEntity: List<GalleryPhotoEntity>)

    @Query("SELECT * FROM gallery_photo")
    fun getGalleryImageAsFlow(): Flow<List<GalleryPhotoEntity>>
}