package com.example.facedetectionusingmlkit.repositories

import com.example.facedetectionusingmlkit.data.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.entity.GalleryPhotoEntity

class MyRepository(
    private val facesAndPhotosDao: FacesAndPhotosDao
) {

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