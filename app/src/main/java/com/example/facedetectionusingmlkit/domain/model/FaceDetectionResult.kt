package com.example.facedetectionusingmlkit.domain.model

import android.graphics.Bitmap

data class FaceDetectionResult(
    val faceBitmap: Bitmap,
    val result: List<Pair<String, String>>
)
