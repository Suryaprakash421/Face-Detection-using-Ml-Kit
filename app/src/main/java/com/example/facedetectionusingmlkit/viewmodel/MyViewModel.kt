package com.example.facedetectionusingmlkit.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.domain.model.AiModel
import com.example.facedetectionusingmlkit.domain.model.FaceDetectionResult
import com.example.facedetectionusingmlkit.domain.usecase.GetDetectedFaceUseCase
import com.example.facedetectionusingmlkit.utils.BitmapCreationMethod
import com.example.facedetectionusingmlkit.utils.FaceDetectionMethods
import com.example.facedetectionusingmlkit.utils.Logger
import com.example.facedetectionusingmlkit.utils.faceDetection.FaceDetector
import com.example.facedetectionusingmlkit.utils.faceDetection.FaceFilterKeys
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
    private val junkFilter: JunkFilter,
    private val faceDetector: FaceDetector
) : AndroidViewModel(application) {

    companion object {
        private const val MY_TAG = "MyViewModel"
    }

    val faceDetectionMode =
        listOf(FaceDetectionMethods.FAST.name, FaceDetectionMethods.ACCURATE.name)

    val bitmapCreationOption =
        listOf(BitmapCreationMethod.COIL.name, BitmapCreationMethod.HEIC_DECODER.name)

    val textRecognitionOption =
        listOf("Latin", "Chinese", "Devanagari", "Japanese", "Korean")

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

    private val _faceResults = MutableStateFlow<List<FaceDetectionResult>>(emptyList())
    val faceResults: StateFlow<List<FaceDetectionResult>> = _faceResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mapping keys to display-friendly titles
    private val displayTitles = mapOf(
        FaceFilterKeys.FACE_INDEX to "Face #",
        FaceFilterKeys.TOTAL_DETECTED_FACES_IN_IMAGE to "Total Faces in Image",
        FaceFilterKeys.FACE_SIZE_VALID to "Size Valid",
        FaceFilterKeys.POSE_VALID to "Pose Valid",
        FaceFilterKeys.EYES_WELL_SEPARATED to "Eyes Separated",
        FaceFilterKeys.IS_BLURRED to "Is Blurry", // True if blurry
        FaceFilterKeys.LAPLACIAN_VARIANCE to "Laplacian Variance",
        FaceFilterKeys.BRIGHTNESS_VALID to "Brightness Valid",
        FaceFilterKeys.IS_FACE_CONSIDERED_CLEAR to "Overall Clear",
        FaceFilterKeys.PROCESSING_ERROR to "Error"
    )

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _faceResults.value = emptyList()
            try {
                @Suppress("DEPRECATION") // Using getBitmap for simplicity, consider modern alternatives for scoped storage if needed
                val bitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<Application>().contentResolver,
                    uri
                )

                // For gallery images, rotation is often 0.
                // For camera captures, you might need to read EXIF data for correct rotation.
                val rotationDegrees = faceDetector.getImageRotationDegrees(uri)

                val results = faceDetector.getClearFaces(bitmap, rotationDegrees)

                if (results.isEmpty() && bitmap != null) {
                    _errorMessage.value =
                        "No face details to display. ML Kit might not have found faces, or all failed processing steps like cropping."
                }
                _faceResults.value = results

            } catch (e: Exception) {
                Log.e("FaceExtractViewModel", "Error processing image", e)
                _errorMessage.value = "Error processing image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDisplayTitle(key: String): String {
        return displayTitles[key] ?: key // Fallback to the key itself if not found
    }
}