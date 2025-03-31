package com.example.facedetectionusingmlkit.data.repositories

import com.example.facedetectionusingmlkit.data.local.dao.FacesAndPhotosDao
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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