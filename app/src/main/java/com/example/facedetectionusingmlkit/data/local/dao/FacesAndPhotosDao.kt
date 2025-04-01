package com.example.facedetectionusingmlkit.data.local.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.facedetectionusingmlkit.data.local.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotoFaceRefEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotosEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FacesAndPhotosDao {

    /**
     *  Get photos which are all contains same face
     *  */
    @Query(
        """
        SELECT P.* FROM Photos P
        JOIN PhotoFaceRef PF ON P.id = PF.photoId
        WHERE PF.faceId = :faceId
    """
    )
    suspend fun getPhotosByFaceId(faceId: UUID): List<PhotosEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGalleryPhotos(galleryPhotoEntity: List<GalleryPhotoEntity>)

    @Query("SELECT * FROM gallery_photo")
    fun getGalleryImageAsFlow(): Flow<List<GalleryPhotoEntity>>

    @Query("SELECT * FROM gallery_photo WHERE isProcessed = 0 ORDER BY capturedDate DESC LIMIT 1500")
    suspend fun getUnProcessedPhotos(): List<GalleryPhotoEntity>

    @Query("UPDATE gallery_photo SET isProcessed = :isProcessed, noOfFaces = :faceCount WHERE fileUri = :photoUri")
    suspend fun updateProcessedPhoto(isProcessed: Boolean, faceCount: Int, photoUri: Uri): Int

    @Query("UPDATE gallery_photo SET isProcessed = 0, noOfFaces = 0")
    suspend fun resetGalleryTable()

    /**
     * Observe faces table changes
     * */
    @Query("SELECT * FROM faces")
    fun getFlowOfFacesEntity(): Flow<List<FacesEntity>>

    /**
     * Insert face detail
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFace(facesEntity: FacesEntity): Long

    /**
     * Insert photo detail
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photosEntity: PhotosEntity): Long

    /**
     * Insert face and photo detail in faceAndPhoto entity
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceAndPhotoDetail(photoFacesEntity: PhotoFaceRefEntity): Long

    /**
     * Clear all tables
     * */
    @Query("DELETE FROM photos")
    suspend fun clearPhotosTable()

    @Query("DELETE FROM faces")
    suspend fun clearFacesTable()

    @Query("DELETE FROM photofaceref")
    suspend fun clearPhotoFaceRefTable()

    @Transaction
    suspend fun clearAllTables() {
        clearPhotoFaceRefTable()
        clearFacesTable()
        clearPhotosTable()
    }
}