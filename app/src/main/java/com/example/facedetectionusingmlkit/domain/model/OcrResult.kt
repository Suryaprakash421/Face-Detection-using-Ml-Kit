package com.example.facedetectionusingmlkit.domain.model

data class OcrResult(
    val ocrText: String = "",
    val labels: String = "",
    val isCartoon: Boolean = false,
    val hasText: Boolean = false,
    val isScreenshot: Boolean = false,
)

data class ImageFilterResult(
    val originalLabelsWithConfidence: Map<String, Float>,
    val hasConfidentHuman: Boolean, // Still useful to know, even if overridden
    val isStronglyCartoonOrArt: Boolean,
    val isPriorityFilterTextHeavy: Boolean, // Renamed to reflect its role
    val isPriorityFilterScreenshot: Boolean, // Renamed
    val isLikelyToy: Boolean, // Primarily for cases where no human and not other priority filters
    val shouldProcessForFaceDetection: Boolean,
    val ocrText: String = ""
)