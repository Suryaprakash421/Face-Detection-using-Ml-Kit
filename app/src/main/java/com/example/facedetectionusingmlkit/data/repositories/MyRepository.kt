package com.example.facedetectionusingmlkit.data.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.facedetectionusingmlkit.data.local.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.local.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotoFaceRefEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotosEntity
import com.example.facedetectionusingmlkit.utils.MediaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class MyRepository(
    private val context: Context,
    private val facesAndPhotosDao: FacesAndPhotosDao
) {

    /**
     * Flow of Entity changes
     * */
    val galleryImages: Flow<List<GalleryPhotoEntity>> = facesAndPhotosDao.getGalleryImageAsFlow()
    val faceListDetails: Flow<List<FacesEntity>> = facesAndPhotosDao.getFlowOfFacesEntity()

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

    /**
     * Insert New face details
     * */
    suspend fun insertFace(facesEntity: FacesEntity) {
        try {
            facesAndPhotosDao.insertFace(facesEntity)
        } catch (e: Exception) {
            Log.e("DataBaseError", "Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Insert New photo details
     * */
    suspend fun insertPhoto(photosEntity: PhotosEntity) {
        try {
            facesAndPhotosDao.insertPhoto(photosEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Insert New face details
     * */
    suspend fun insertFaceAndPhotoDetail(photoFaceRefEntity: PhotoFaceRefEntity) {
        try {
            facesAndPhotosDao.insertFaceAndPhotoDetail(photoFaceRefEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear all tables
     * */
    suspend fun clearAllTables() {
        try {
            facesAndPhotosDao.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear all tables
     * */
    suspend fun getPhotosByFaceId(faceId: UUID): List<PhotosEntity> = withContext(Dispatchers.IO) {
        try {
            facesAndPhotosDao.getPhotosByFaceId(faceId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}