package com.example.facedetectionusingmlkit.domain.model

data class OcrResult(
    val ocrText: String = "",
    val labels: String = "",
    val isCartoon: Boolean = false,
    val hasText: Boolean = false,
    val isScreenshot: Boolean = false,
)