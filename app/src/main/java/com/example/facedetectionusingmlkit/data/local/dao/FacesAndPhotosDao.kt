package com.example.facedetectionusingmlkit.data.local.dao

import android.net.Uri
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

    @Query("SELECT * FROM gallery_photo WHERE isProcessed = 0 ORDER BY capturedDate DESC")
    suspend fun getUnProcessedPhotos(): List<GalleryPhotoEntity>

    @Query("UPDATE gallery_photo SET isProcessed = :isProcessed, noOfFaces = :faceCount WHERE fileUri = :photoUri")
    suspend fun updateProcessedPhoto(isProcessed: Boolean, faceCount: Int, photoUri: Uri): Int

    @Query("UPDATE gallery_photo SET isProcessed = 0, noOfFaces = 0")
    suspend fun resetGalleryTable()

}