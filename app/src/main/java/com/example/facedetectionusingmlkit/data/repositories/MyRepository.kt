package com.example.facedetectionusingmlkit.data.repositories

import android.content.Context
import android.net.Uri
import com.example.facedetectionusingmlkit.data.local.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.utils.MediaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MyRepository(
    private val context: Context,
    private val facesAndPhotosDao: FacesAndPhotosDao
) {

    /**
     * Flow of Entity changes
     * */
    val galleryImages: Flow<List<GalleryPhotoEntity>> = facesAndPhotosDao.getGalleryImageAsFlow()

    /**
     * Insert gallery images
     * */
    suspend fun insertGalleryImages(galleryPhotoEntity: List<GalleryPhotoEntity>) {
        try {
            facesAndPhotosDao.insertGalleryPhotos(galleryPhotoEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get gallery images from MediaStore
     * */
    suspend fun getLocalImages(): List<GalleryPhotoEntity> = withContext(Dispatchers.IO) {
        try {
            MediaHelper.getGalleryPhotos(context)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get gallery images from MediaStore
     * */
    suspend fun getUnProcessedPhotos(): List<GalleryPhotoEntity> = withContext(Dispatchers.IO) {
        try {
            facesAndPhotosDao.getUnProcessedPhotos()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Update isProcessed and face count
     * */
    suspend fun updateProcessedPhoto(isProcessed: Boolean, noOfFaces: Int, photoUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                facesAndPhotosDao.updateProcessedPhoto(isProcessed, noOfFaces, photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Reset gallery Table
     * */
    suspend fun resetGalleryTable() {
        withContext(Dispatchers.IO) {
            try {
                facesAndPhotosDao.resetGalleryTable()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}