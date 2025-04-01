package com.example.facedetectionusingmlkit.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.utils.Config
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
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

@HiltWorker
class FaceDetectionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val myRepository: MyRepository,
    private val prefManager: PrefManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORKER_NAME = "face_detection_worker"
        const val MY_TAG = "FaceDetectionWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            startFaceDetection()
            Result.success()
        } catch (e: Exception) {
            Log.e(MY_TAG, "Exception: ${e.message}")
            Result.failure()
        }
    }

    private val option = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()

    private val faceDetector by lazy { FaceDetection.getClient(option) }

    private suspend fun startFaceDetection() {
        Log.d(MY_TAG, "Worker started")
        val galleryPhotos = myRepository.getUnProcessedPhotos()

        Log.d(MY_TAG, "galleryPhotos: ${galleryPhotos.size}")
        withContext(Dispatchers.Default) {
            galleryPhotos.chunked(Config.PARALLEL_COUNT).forEach { chunk ->
                Log.d(MY_TAG, "chunked: ${chunk.size}")

                val detectors = List(chunk.size) {
                    FaceDetection.getClient(
                        FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                            .build()
                    )
                }

                measureTimeMillis {
                    chunk.mapIndexed { index, photo ->
                        Log.d(MY_TAG, "photoName: ${photo.photoName} -- $index")
                        async {
                            val detector = detectors[index]
                            var bitmap: Bitmap?
                            measureTimeMillis {
                                bitmap = loadImageAsBitmap(photo.fileUri) ?: return@async
                            }.also {
                                Log.i(MY_TAG, "Takes $it ms to create bitmap for ${photo.photoName}")
                            }
                            try {
                                val faces = runMlKit(bitmap!!, 0, detector)
                                Log.d(MY_TAG, "photoName: ${photo.photoName}, faces: ${faces.size}")
                                updateProcessedPhoto(faces.size, photo.fileUri)
                            } finally {
                                bitmap?.recycle()
                                detector.close()
                                bitmap = null
                            }
                        }
                    }.awaitAll()
                }.also {
                    prefManager.addProcessedTime(it)
                    Log.i(
                        MY_TAG,
                        "$it ms for ${chunk.size} -- avg time = ${prefManager.getAverageProcessedTime()} ms"
                    )
                }
            }
        }
    }


    private suspend fun loadImageAsBitmap(photoUri: Uri): Bitmap? {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(photoUri)
            .size(640, 640)
            .allowHardware(true)
            .build()

        return (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
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
