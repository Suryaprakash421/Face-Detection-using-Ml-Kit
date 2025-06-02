package com.example.facedetectionusingmlkit.utils.faceDetection

object FaceFilterKeys {
    const val TOTAL_DETECTED_FACES_IN_IMAGE = "TotalDetectedFacesInImage"
    const val FACE_SIZE_VALID = "IsFaceSizeValid"
    const val POSE_VALID = "IsPoseValid"
    const val EYES_WELL_SEPARATED = "AreEyesWellSeparated"
    const val IS_BLURRED = "IsBlurred" // True if blurry, False if clear
    const val LAPLACIAN_VARIANCE = "LaplacianVariance"
    const val BRIGHTNESS_VALID = "IsBrightnessValid"
    const val IS_FACE_CONSIDERED_CLEAR = "IsFaceConsideredClear" // Overall status for this face
    const val PROCESSING_ERROR = "ProcessingError"
    const val FACE_INDEX = "FaceIndex"
}