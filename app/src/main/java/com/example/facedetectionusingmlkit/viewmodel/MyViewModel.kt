package com.example.facedetectionusingmlkit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.workmanager.startWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    application: Application,
    private val myRepository: MyRepository,
    private val workManager: WorkManager
) : AndroidViewModel(application) {

    /**
     * Flow of Entity changes
     * */
    val galleryImages: Flow<List<GalleryPhotoEntity>> = myRepository.galleryImages

    /**
     * Insert gallery images
     * */
    fun insertGalleryImages(galleryPhotoEntity: List<GalleryPhotoEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            myRepository.insertGalleryImages(galleryPhotoEntity)
        }
    }

    /**
     * Start face detection worker
     * */
    fun startFaceDetectionWorker(){
        workManager.startWorker()
    }
}