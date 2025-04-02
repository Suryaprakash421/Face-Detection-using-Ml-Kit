package com.example.facedetectionusingmlkit.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.domain.model.AiModel
import com.example.facedetectionusingmlkit.domain.usecase.GetDetectedFaceUseCase
import com.example.facedetectionusingmlkit.utils.BitmapCreationMethod
import com.example.facedetectionusingmlkit.utils.FaceDetectionMethods
import com.example.facedetectionusingmlkit.workmanager.FaceDetectionWorker
import com.example.facedetectionusingmlkit.workmanager.startWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    application: Application,
    private val myRepository: MyRepository,
    private val workManager: WorkManager,
    private val prefManager: PrefManager,
    private val getDetectedFaceUseCase: GetDetectedFaceUseCase
) : AndroidViewModel(application) {

    val faceDetectionMode =
        listOf(FaceDetectionMethods.FAST.name, FaceDetectionMethods.ACCURATE.name)

    val bitmapCreationOption =
        listOf(BitmapCreationMethod.COIL.name, BitmapCreationMethod.HEIC_DECODER.name)

    /**
     * Flow of Entity changes
     * */
    val galleryImages: Flow<List<GalleryPhotoEntity>> = myRepository.galleryImages
    val faceAndPhotoList: StateFlow<List<AiModel>> = getDetectedFaceUseCase.aiScreenData

    /**
     * Insert gallery images
     * */
    fun insertGalleryImages(galleryPhotoEntity: List<GalleryPhotoEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            myRepository.insertGalleryImages(galleryPhotoEntity)
        }
    }

    /**
     * Reset gallery Table
     * */
    fun resetGalleryTable() {
        viewModelScope.launch {
            prefManager.resetProcessedTime()
            prefManager.resetMemUsage()
            myRepository.resetGalleryTable()
            myRepository.clearAllTables()
            stopFaceDetectionWorker()
        }
    }

    /**
     * Start face detection worker
     * */
    fun startFaceDetectionWorker() {
        workManager.startWorker()
    }

    fun stopFaceDetectionWorker() {
        workManager.cancelUniqueWork(FaceDetectionWorker.WORKER_NAME)
    }

    init {
        observeWorkerState()
    }

    private var _workerState = MutableStateFlow<WorkInfo.State?>(null)
    val workerState: MutableStateFlow<WorkInfo.State?> get() = _workerState

    private fun observeWorkerState() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(FaceDetectionWorker.WORKER_NAME)
                .collectLatest { workInfo ->
                    if (workInfo.isNotEmpty()) { // Check if list is not empty
                        val state = workInfo[0].state
                        workerState.value = state
                    }
                }
        }
    }

    /**
     * Get gallery images from MediaStore
     * */
    private var _localImages = MutableStateFlow<List<GalleryPhotoEntity>>(emptyList())
    val localImages: StateFlow<List<GalleryPhotoEntity>> get() = _localImages.asStateFlow()

    fun getLocalImages() {
        Log.d("isGranted", "getLocalImages -- entered")
        viewModelScope.launch {
            val images = myRepository.getLocalImages()
            Log.d("isGranted", "images -- ${images.size}")
            _localImages.value = images
        }
    }

}