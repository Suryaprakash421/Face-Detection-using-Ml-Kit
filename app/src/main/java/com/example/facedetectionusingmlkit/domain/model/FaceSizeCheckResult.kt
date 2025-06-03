package com.example.facedetectionusingmlkit.domain.model

data class FaceSizeCheckResult(
    val facePercentage: Float,
    val isValidFace: Boolean,
    val faceWidth: Int,
    val faceHeight: Int,
    val minSize: Int,
    val minPercentageRequired: Float,
)
