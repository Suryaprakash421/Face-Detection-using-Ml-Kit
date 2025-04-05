package com.example.facedetectionusingmlkit.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.utils.HeicDecoderUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

@HiltWorker
class FaceDetectionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val myRepository: MyRepository,
    private val prefManager: PrefManager,
    private val faceRecognition: FaceRecognition
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORKER_NAME = "face_detection_worker"
        const val MY_TAG = "FaceDetectionWorker"
    }

    private val semaphore = Semaphore(Config.PARALLEL_COUNT)

    override suspend fun doWork(): Result {
        return try {
            var iteration = 0
            while (!isStopped) {
                iteration++
                val galleryImages = getImage(Config.BATCH_SIZE)
                Log.i(MY_TAG, "iteration: $iteration with ${galleryImages.size}")
                if (galleryImages.isEmpty()) {
                    break
                }
                startFaceDetection(galleryImages)
                delay(2000)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(MY_TAG, "Error: ${e.message}, retrying...")
            Result.retry()
        }
    }

    private val faceDetectionMode =
        if (prefManager.isFaceDetectionModeAccurate()) FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE else FaceDetectorOptions.PERFORMANCE_MODE_FAST

    private val option = FaceDetectorOptions.Builder()
        .setPerformanceMode(faceDetectionMode)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setMinFaceSize(prefManager.getMinFaceSize())
        .build()

    private val faceDetector by lazy { FaceDetection.getClient(option) }

    private suspend fun getImage(batchSize: Int): List<GalleryPhotoEntity> {
        return myRepository.getUnProcessedPhotos(batchSize)
    }

    private suspend fun startFaceDetection(galleryPhotos: List<GalleryPhotoEntity>): Boolean {
        Log.d(MY_TAG, "Worker started with ${galleryPhotos.size} photos")

        return try {
            val shouldUseHeicDecoder = prefManager.isHeicDecoder()
            Log.d(MY_TAG, "galleryPhotos: ${galleryPhotos.size}")
            withContext(Dispatchers.Default) {
//                galleryPhotos.chunked(Config.BATCH_SIZE).forEach { chunk ->
                var processed = 0
                val processedPhotos = mutableSetOf<Pair<Uri, Int>>()
                measureTimeMillis {
                    galleryPhotos.mapIndexed { index, photo ->
                        Log.d(MY_TAG, "photoName: ${photo.photoName} -- $index")
                        async {
                            semaphore.withPermit {
                                measureTimeMillis {
                                    processed += 1
                                    Log.d(
                                        MY_TAG,
                                        "photoName: ${photo.photoName} inside semaphore"
                                    )
                                    var bitmap: Bitmap?
                                    val mimeType =
                                        context.contentResolver.getType(photo.fileUri)
                                    measureTimeMillis {
                                        bitmap = if (shouldUseHeicDecoder) {
                                            Log.i(MY_TAG, "Inside Heic decoder")
                                            HeicDecoderUtil.decodeBitmap(
                                                context = context,
                                                photo.fileUri,
                                                1080
                                            )
                                        } else {
                                            Log.i(MY_TAG, "Inside Coil bitmap creation")
                                            loadImageAsBitmap(photo.fileUri)
                                        } ?: run {
                                            processedPhotos.add(Pair(photo.fileUri, 0))
//                                            updateProcessedPhoto(0, photo.fileUri)
                                            return@async
                                        }

                                    }.also {
                                        prefManager.addSingleImageProcessTime(it, mimeType)
                                        Log.i(
                                            MY_TAG,
                                            "Takes $it ms to create bitmap for ${photo.photoName} -- mimeType: $mimeType"
                                        )
                                    }
                                    bitmap =
                                        bitmap?.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                                    try {
                                        val faces = runMlKit(bitmap!!, 0, faceDetector)
                                        Log.d(
                                            MY_TAG,
                                            "photoName: ${photo.photoName}, faces: ${faces.size}"
                                        )
                                        if (faces.isNotEmpty()) {
                                            faceRecognition.processDetectedFace(
                                                faces,
                                                bitmap!!,
                                                photo
                                            )
                                        }
                                        processedPhotos.add(Pair(photo.fileUri, faces.size))
//                                        updateProcessedPhoto(faces.size, photo.fileUri)
                                    } finally {
                                        bitmap?.recycle()
                                        bitmap = null
                                    }
                                }.also {
                                    val mem = getMemoryUsageMB()
                                    prefManager.addMemoryUsage(mem)
                                }
                            }
                        }
                    }.awaitAll()
                    updateProcessedPhotos(processedPhotos)
                }.also {
                    prefManager.addProcessedTime(it)
                    Log.i(
                        MY_TAG,
                        "$it ms for ${galleryPhotos.size} photos-- avg time = ${prefManager.getAverageProcessedTime()} ms"
                    )
                }
//                }
            }
            true
        } catch (e: Exception) {
            Log.e(MY_TAG, "Exception in face detection: ${e.message}")
            false
        }
    }

    private suspend fun updateProcessedPhotos(processedPhotos: MutableSet<Pair<Uri, Int>>) {
        val photoUris = processedPhotos.map { it.first }
        val existingMap = myRepository.getPhotosByUriList(photoUris).associateBy { it.fileUri }

        val updatedEntities = processedPhotos.mapNotNull { (uri, faceCount) ->
            existingMap[uri]?.copy(
                isProcessed = true,
                noOfFaces = faceCount
            )
        }
        myRepository.updateGalleryPhotos(updatedEntities)
    }

    /**
     * Memory
     * */
    fun getMemoryUsageMB(): Double {
        return try {
            val reader = RandomAccessFile("/proc/self/statm", "r")
            val memUsageKB =
                reader.readLine().split(" ")[1].toLong() * 4 // Convert pages to KB (1 page = 4KB)
            reader.close()

            memUsageKB / 1024.0 // Convert to MB
        } catch (e: Exception) {
            e.printStackTrace()
            -1.0
        }
    }

    // Helper function to calculate inSampleSize for scaling
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of the image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    suspend fun loadAndConvertToJpegBitmap(filePath: String): Bitmap? {
        return try {
            // Load image as Bitmap using Coil (supports HEIC, PNG, JPG, etc.)
            val bitmap = ImageLoader(context)
                .execute(
                    ImageRequest.Builder(context)
                        .data(File(filePath))
                        .size(512) // Resize for performance
                        .allowHardware(false) // Avoid GPU Bitmap (convertible)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .build()
                ).drawable?.toBitmap() ?: return null

            // Convert to JPEG format Bitmap (in-memory)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            // Decode back to Bitmap to ensure it's in JPEG format
            val jpegBitmap = BitmapFactory.decodeByteArray(
                outputStream.toByteArray(),
                0,
                outputStream.size()
            )
            outputStream.close()

            jpegBitmap
        } catch (e: Exception) {
            Log.e("ImageConversion", "Error converting image to JPEG Bitmap: ${e.message}")
            null
        }
    }

    private suspend fun loadImageAsBitmap(photoUri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(photoUri)
            .size(480, 360)
            .allowHardware(false)
            .build()

        return@withContext (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
    }

    /**
     * Create bitmap for selected image using Glide
     * */
    private suspend fun createBitmapUsingGlide(filePath: String): Bitmap? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(File(filePath))
                    .submit(640, 640)
                    .get()

                bitmap
            } catch (e: Exception) {
                Log.e("DetectedFace", "Exception while creating bitmap: ${e.message}")
                null
            }
        }

    private suspend fun runMlKit(
        bitmap: Bitmap,
        imageRotation: Int,
        detector: FaceDetector
    ): List<Face> = suspendCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, imageRotation)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    continuation.resume(faces)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    private suspend fun updateProcessedPhoto(noOfFaces: Int, photoUri: Uri) {
        withContext(Dispatchers.IO) {
            myRepository.updateProcessedPhoto(true, noOfFaces, photoUri)
        }
    }

}
