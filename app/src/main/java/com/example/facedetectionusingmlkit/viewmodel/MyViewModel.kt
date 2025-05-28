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
import com.example.facedetectionusingmlkit.utils.Logger
import com.example.facedetectionusingmlkit.utils.junkFilter.JunkFilter
import com.example.facedetectionusingmlkit.workmanager.FaceDetectionWorker
import com.example.facedetectionusingmlkit.workmanager.startWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    application: Application,
    private val myRepository: MyRepository,
    private val workManager: WorkManager,
    private val prefManager: PrefManager,
    private val getDetectedFaceUseCase: GetDetectedFaceUseCase,
    private val junkFilter: JunkFilter
) : AndroidViewModel(application) {

    companion object {
        private const val MY_TAG = "MyViewModel"
    }

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
            Logger.deleteLogFile()
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
        viewModelScope.launch {
            val images = myRepository.getLocalImages()
            _localImages.value = images
        }
    }

    /**
     * Get gallery images from MediaStore
     * */
    private var _whatsAppImages = MutableStateFlow<List<GalleryPhotoEntity>>(emptyList())
    val whatsAppImages: StateFlow<List<GalleryPhotoEntity>> get() = _whatsAppImages.asStateFlow()

    fun getWhatsAppPhotos() {
        Log.d("isGranted", "getWhatsAppPhotos -- entered")
        viewModelScope.launch(Dispatchers.IO) {
            val images = myRepository.getWhatsAppPhotos()
            Log.d("isGranted", "images -- ${images.size}")
            _whatsAppImages.value = images
            filterJunks()
        }
    }

    /**
     * Get gallery images from MediaStore
     * */
    private var _junkFiltered = MutableStateFlow(setOf<GalleryPhotoEntity>())
    val junkFiltered: StateFlow<Set<GalleryPhotoEntity>> get() = _junkFiltered.asStateFlow()

    private fun addFilteredImage(new: GalleryPhotoEntity) {
        _junkFiltered.update { old ->
            old + new
        }
    }

    private var inProgress = false
    private var oldImageSize = 0
    private var processedImages: MutableSet<GalleryPhotoEntity> = mutableSetOf()
    private val semaphore = Semaphore(4)
    fun filterJunks() {
        if (inProgress || oldImageSize == whatsAppImages.value.size) return
        inProgress = true
        Logger.d(MY_TAG, "Filter started")
        viewModelScope.launch {
            val list = whatsAppImages.value - processedImages

//            val jobs = list.map { photo ->
//                async { // run each task concurrently
//                    semaphore.withPermit {
//                        Logger.d(MY_TAG, "photoName: ${photo.photoName} -- Check valid photo")
//                        val isValid = junkFilter.isRelevantFamilyOrFriendPhoto(photo)
//                        if (isValid) {
//                            addFilteredImage(photo)
//                        }
//                        processedImages.add(photo)
//                    }
//                }
//            }.awaitAll()

            list.chunked(4).map { chunk ->
                chunk.map { photo ->
                    async { // run each task concurrently
                        Logger.d(MY_TAG, "photoName: ${photo.photoName} -- Check valid photo")
                        val isValid = junkFilter.isRelevantFamilyOrFriendPhoto(photo)
                        if (isValid) {
                            addFilteredImage(photo)
                        }
                        processedImages.add(photo)
                    }
                }.awaitAll()
            }

            oldImageSize = whatsAppImages.value.size
            inProgress = false
            Logger.d(MY_TAG, "Filter ended")
        }
    }

}