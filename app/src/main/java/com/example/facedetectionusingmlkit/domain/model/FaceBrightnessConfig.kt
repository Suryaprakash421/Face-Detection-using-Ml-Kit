package com.example.facedetectionusingmlkit.domain.model

data class FaceBrightnessConfig(
    // For Shadow Clipping
    val absoluteShadowLuminance: Int = 15, // Luminance below this is considered deep/true shadow
    val maxShadowPercentage: Float = 0.30f, // If >30% of pixels are deep shadow, image is too dark

    // For Highlight Clipping
    val absoluteHighlightLuminance: Int = 240, // Luminance above this is considered blown-out/true highlight
    val maxHighlightPercentage: Float = 0.15f, // If >15% of pixels are blown-out, image is overexposed

    // For Dynamic Range/Contrast within the face (after accounting for extreme clipping)
    val dynamicRangeMinPercentile: Float = 0.05f, // e.g., 5th percentile of luminance values
    val dynamicRangeMaxPercentile: Float = 0.95f, // e.g., 95th percentile of luminance values
    val minRequiredDynamicRange: Int = 50 // Minimum difference (spread) required between these percentiles
    // A small spread means low contrast/flat image
)
