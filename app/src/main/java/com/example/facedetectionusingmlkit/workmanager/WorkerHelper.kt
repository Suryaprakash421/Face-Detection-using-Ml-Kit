package com.example.facedetectionusingmlkit.workmanager

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

fun WorkManager.startWorker() {
    val workRequest = OneTimeWorkRequestBuilder<FaceDetectionWorker>().build()
    Log.d("FaceDetectionWorker", "HELPER -- Entered")

    enqueueUniqueWork(
        FaceDetectionWorker.WORKER_NAME,
        ExistingWorkPolicy.KEEP,
        workRequest
    )
}