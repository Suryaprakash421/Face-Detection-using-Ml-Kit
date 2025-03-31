package com.example.facedetectionusingmlkit.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FaceDetectionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val myRepository: MyRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            startFaceDetection()
            Result.success()
        } catch (e: Exception) {
            Log.e("FaceDetectionWorker", "Exception: ${e.message}")
            Result.failure()
        }
    }

    private fun startFaceDetection() {
        Log.d("FaceDetectionWorker", "Worker started with repository: $")
//        val galleryPhotos =

    }
}
