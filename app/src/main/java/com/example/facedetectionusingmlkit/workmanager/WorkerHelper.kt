package com.example.facedetectionusingmlkit.workmanager

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun WorkManager.startWorker() {
    val workRequest = OneTimeWorkRequestBuilder<FaceDetectionWorker>()
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            30,
            TimeUnit.SECONDS
        )
        .build()
    Log.d("FaceDetectionWorker", "HELPER -- Entered")

    enqueueUniqueWork(
        FaceDetectionWorker.WORKER_NAME,
        ExistingWorkPolicy.KEEP,
        workRequest
    )
}