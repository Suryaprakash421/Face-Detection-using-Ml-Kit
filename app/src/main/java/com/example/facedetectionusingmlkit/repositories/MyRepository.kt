package com.example.facedetectionusingmlkit.repositories

import com.example.facedetectionusingmlkit.data.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.entity.GalleryPhotoEntity
import kotlinx.coroutines.flow.Flow

class MyRepository(
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
}