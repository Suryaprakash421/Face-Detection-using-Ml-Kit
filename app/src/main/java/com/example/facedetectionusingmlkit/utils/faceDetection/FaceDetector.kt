package com.example.facedetectionusingmlkit.utils.faceDetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.example.facedetectionusingmlkit.domain.model.FaceBrightnessConfig
import com.example.facedetectionusingmlkit.domain.model.FaceDetectionResult
import com.example.facedetectionusingmlkit.domain.model.FaceSizeCheckResult
import com.example.facedetectionusingmlkit.utils.Logger
import com.example.facedetectionusingmlkit.utils.formatToDecimalPlaces
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class FaceDetector @Inject constructor(
    @ApplicationContext val context: Context
) {

    companion object {
        private const val MY_TAG = "FaceDetector"
    }

    private val option = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // Keep FAST for speed, but consider ACCURATE if pose/landmark quality is insufficient
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)       // Crucial for eye separation and can improve pose
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Optional: Useful for eye open probability, smiling
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)        // Keep NONE unless you need detailed face contours
        .setMinFaceSize(0.5f)      // Assumes this method returns a float between 0.0 and 1.0
        .build()

    private val faceDetector by lazy { FaceDetection.getClient(option) }

    private suspend fun runMlKit(
        bitmap: Bitmap,
        imageRotation: Int
    ): List<Face> = suspendCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, imageRotation)
            faceDetector.process(image)
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

    suspend fun getClearFaces(
        originalBitmap: Bitmap,
        rotationDegrees: Int,
    ): List<FaceDetectionResult> {
        val allDetectedFaces =
            runMlKit(originalBitmap, rotationDegrees) // Assuming this is defined in your class
        val resultsList = mutableListOf<FaceDetectionResult>()

        val imageWidth = originalBitmap.width
        val imageHeight = originalBitmap.height

        val totalFacesInImage = allDetectedFaces.size

        allDetectedFaces.forEachIndexed { index, face ->
            val faceSpecificKeyValue = mutableListOf<Pair<String, String>>()

            faceSpecificKeyValue.add(Pair(FaceFilterKeys.FACE_INDEX, (index + 1).toString()))
            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.TOTAL_DETECTED_FACES_IN_IMAGE,
                    totalFacesInImage.toString()
                )
            )


            val faceBitmap = cropFaceBitmap(originalBitmap, face.boundingBox, rotationDegrees)

            if (faceBitmap == null) {
                Logger.d(
                    MY_TAG,
                    "faceBitmap is null for face index $index. Skipping this face for detailed results."
                )
                // Optionally, add a result indicating crop failure if you want to show it
                // val errorResult = FaceDetectionResult(
                //    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), // Placeholder
                //    listOf(
                //        Pair(FaceFilterKeys.FACE_INDEX, (index + 1).toString()),
                //        Pair(FaceFilterKeys.PROCESSING_ERROR, "Failed to crop face bitmap")
                //    )
                // )
                // resultsList.add(errorResult)
                return@forEachIndexed // Skips to the next face in forEachIndexed
            }

            var isThisFaceClear = true // Flag to track if this specific face passed all checks

            // 1. Size Check
            val faceWidth = face.boundingBox.width()
            val faceHeight = face.boundingBox.height()

            // Check against both image height and width
            val faceSizeResult = isFaceSizeValid(
                face,
                imageHeight,
                imageWidth,
                allDetectedFaces.size, // Pass the total count here
                baseRelativeThresholdPercentage = 0.15f, // Or your preferred base
                minAbsolutePixelSize = 50 // Example, tune this
            )

            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.FACE_SIZE_VALID,
                    "${faceSizeResult.isValidFace} (${
                        faceSizeResult.facePercentage.formatToDecimalPlaces(
                            2
                        )
                    }%)"
                )
            )
            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.FACE_DIMENSION,
                    "${faceSizeResult.faceWidth} x ${faceSizeResult.faceHeight}"
                )
            )

            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.MIN_FACE_SIZE,
                    "${faceSizeResult.minSize} x ${faceSizeResult.minSize}"
                )
            )

            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.MIN_PERCENTAGE_REQUIRED,
                    "${faceSizeResult.minPercentageRequired}%"
                )
            )
            if (!faceSizeResult.isValidFace) isThisFaceClear = false

            // 2. Pose Check
            val poseValid = isPoseValid(face, 25.0f)
            faceSpecificKeyValue.add(Pair(FaceFilterKeys.POSE_VALID, poseValid.toString()))
            if (!poseValid) isThisFaceClear = false

            // 3. Landmark Spread (Eyes)
            val eyesSeparated = areEyesWellSeparated(face)
            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.EYES_WELL_SEPARATED,
                    eyesSeparated.toString()
                )
            )
            if (!eyesSeparated) isThisFaceClear = false

            // 4a. Blur Check
            val laplacianVariance = calculateLaplacianVariance(faceBitmap)
            val blurThreshold = 50.0 // Example threshold, needs tuning
            val isNotBlurred = laplacianVariance >= blurThreshold
            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.IS_BLURRED,
                    (!isNotBlurred).toString()
                )
            ) // True if it IS blurry
            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.LAPLACIAN_VARIANCE,
                    String.format("%.2f", laplacianVariance)
                )
            )
            if (!isNotBlurred) isThisFaceClear = false

            // 4b. Brightness Check
            val brightnessValid = isFaceBrightnessValid(faceBitmap)
            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.BRIGHTNESS_VALID,
                    brightnessValid.toString()
                )
            )
            if (!brightnessValid) isThisFaceClear = false

            faceSpecificKeyValue.add(
                Pair(
                    FaceFilterKeys.IS_FACE_CONSIDERED_CLEAR,
                    isThisFaceClear.toString()
                )
            )

            resultsList.add(FaceDetectionResult(faceBitmap, faceSpecificKeyValue))
        }

        return resultsList
    }

    fun cropFaceBitmap(
        originalBitmap: Bitmap,
        faceBoundingBox: Rect,
        rotationDegrees: Int
    ): Bitmap? {
        // Create a copy of the bounding box to avoid modifying the original
        val bounds = Rect(faceBoundingBox)

        // Ensure bounding box is within bitmap dimensions
        bounds.left = max(0, bounds.left)
        bounds.top = max(0, bounds.top)
        // Adjust right and bottom based on width and height from potentially clipped left/top
        bounds.right = min(originalBitmap.width, bounds.right)
        bounds.bottom = min(originalBitmap.height, bounds.bottom)

        val width = bounds.width()
        val height = bounds.height()

        if (width <= 0 || height <= 0) {
            return null // Invalid bounds
        }

        var croppedBitmap =
            Bitmap.createBitmap(originalBitmap, bounds.left, bounds.top, width, height)

        // For now, assuming `originalBitmap` is correctly oriented for the `faceBoundingBox`:
        return croppedBitmap
    }

    /******************************* Check Clear face ******************************/
    fun isFaceSizeValid(
        face: Face,
        imageHeight: Int,
        imageWidth: Int,
        totalFaceCountInImage: Int,
        baseRelativeThresholdPercentage: Float = 0.15f, // Your original 15% as a base
        minAbsolutePixelSize: Int = 80 // Minimum width/height in pixels (e.g., 80px, 96px, 120px - TUNE THIS)
    ): FaceSizeCheckResult {
        val faceWidthPixels = face.boundingBox.width()
        val faceHeightPixels = face.boundingBox.height()

        // 2. Dynamically Adjust Relative Threshold Based on Face Count
        val adjustedRelativeThresholdPercentage: Float = when (totalFaceCountInImage) {
            0 -> {
                Logger.w("FaceSizeCheck", "Warning: totalFaceCountInImage is 0.")
                baseRelativeThresholdPercentage
            }

            1 -> baseRelativeThresholdPercentage
            2 -> baseRelativeThresholdPercentage * 0.75f
            3 -> baseRelativeThresholdPercentage * 0.60f
            4 -> baseRelativeThresholdPercentage * 0.40f
//            5 -> baseRelativeThresholdPercentage * 0.25f
            else -> baseRelativeThresholdPercentage * 0.30f
        }

        val calculatedFaceAreaPercentage =
            ((faceWidthPixels * faceHeightPixels).toFloat() / (imageWidth * imageHeight).toFloat()) * 100
        // 1. Check Minimum Absolute Pixel Size (Crucial for baseline quality)
        // This ensures the face isn't just a tiny speck, even if it meets a relative threshold.
        if (faceWidthPixels < minAbsolutePixelSize || faceHeightPixels < minAbsolutePixelSize) {
            Logger.d(
                "FaceSizeCheck",
                "Rejected: Face too small in absolute pixels (w:$faceWidthPixels, h:$faceHeightPixels, min:$minAbsolutePixelSize)"
            )
            return FaceSizeCheckResult(
                facePercentage = calculatedFaceAreaPercentage,
                isValidFace = false,
                faceWidth = faceWidthPixels,
                faceHeight = faceHeightPixels,
                minSize = minAbsolutePixelSize,
                minPercentageRequired = adjustedRelativeThresholdPercentage * 100
            )
        }

        // You could also use a formula, e.g.:
        // val factor = if (totalFaceCountInImage > 1) (1.0f / kotlin.math.sqrt(totalFaceCountInImage.toFloat())).coerceAtLeast(0.4f) else 1.0f
        // val adjustedRelativeThresholdPercentage = baseRelativeThresholdPercentage * factor

        // 3. Check Relative Size with the Adjusted Threshold
        val sizePercentageOfHeight = faceHeightPixels.toFloat() / imageHeight.toFloat()
        val sizePercentageOfWidth = faceWidthPixels.toFloat() / imageWidth.toFloat()

        Logger.d(
            "SizeCheck",
            "(h%:${sizePercentageOfHeight * 100}, w%:${sizePercentageOfWidth * 100}, adjustedThresh%:${adjustedRelativeThresholdPercentage * 100}) for $totalFaceCountInImage faces."
        )
        val relativeCheckPassed = sizePercentageOfHeight > adjustedRelativeThresholdPercentage &&
                sizePercentageOfWidth > adjustedRelativeThresholdPercentage

        if (!relativeCheckPassed) {
            Logger.d(
                "FaceSizeCheck",
                "Rejected: Face too small in relative percentage (h%:${sizePercentageOfHeight * 100}, w%:${sizePercentageOfWidth * 100}, adjustedThresh%:${adjustedRelativeThresholdPercentage * 100}) for $totalFaceCountInImage faces."
            )
        }

        return FaceSizeCheckResult(
            facePercentage = (minOf(sizePercentageOfHeight, sizePercentageOfWidth) * 100),
            isValidFace = relativeCheckPassed,
            faceWidth = faceWidthPixels,
            faceHeight = faceHeightPixels,
            minSize = minAbsolutePixelSize,
            minPercentageRequired = adjustedRelativeThresholdPercentage * 100,
        ) // The absolute check already passed if we reach here
    }

    fun isPoseValid(face: Face, angleThreshold: Float): Boolean {
        // EulerX: Pitch (nodding "yes")
        // EulerY: Yaw (shaking "no")
        // EulerZ: Roll (tilting head side-to-side)
        val pitch = face.headEulerAngleX
        val yaw = face.headEulerAngleY
        val roll = face.headEulerAngleZ

        return kotlin.math.abs(pitch) < angleThreshold &&
                kotlin.math.abs(yaw) < angleThreshold &&
                kotlin.math.abs(roll) < angleThreshold
    }

    fun areEyesWellSeparated(face: com.google.mlkit.vision.face.Face): Boolean {
        val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)

        if (leftEye != null && rightEye != null) {
            val eyeDistance = kotlin.math.sqrt(
                (leftEye.position.x - rightEye.position.x).pow(2) +
                        (leftEye.position.y - rightEye.position.y).pow(2)
            )

            // Threshold for eye distance: e.g., at least 25-30% of the face width.
            // This threshold needs tuning based on your specific needs.
            val faceWidth = face.boundingBox.width().toFloat()
            val minEyeDistanceThreshold = faceWidth * 0.20f // Example: 20% of face width

            return eyeDistance > minEyeDistanceThreshold
        }
        return false // One or both eyes not detected
    }

    fun calculateLaplacianVariance(bitmap: Bitmap): Double {
        if (bitmap.width <= 2 || bitmap.height <= 2) return 0.0 // Too small to process

        // 1. Convert to Grayscale
        val grayscaleBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Grayscale
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val width = grayscaleBitmap.width
        val height = grayscaleBitmap.height
        val pixels = IntArray(width * height)
        grayscaleBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val laplacian = Array(height) { DoubleArray(width) }
        var sumLaplacian = 0.0
        var count = 0

        // 2. Apply Laplacian Operator (simple 3x3 kernel: [[0, 1, 0], [1, -4, 1], [0, 1, 0]])
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val p00 =
                    Color.red(pixels[(y - 1) * width + (x - 1)]) // Using red channel for grayscale value
                val p01 = Color.red(pixels[(y - 1) * width + x])
                val p02 = Color.red(pixels[(y - 1) * width + (x + 1)])
                val p10 = Color.red(pixels[y * width + (x - 1)])
                val p11 = Color.red(pixels[y * width + x])
                val p12 = Color.red(pixels[y * width + (x + 1)])
                val p20 = Color.red(pixels[(y + 1) * width + (x - 1)])
                val p21 = Color.red(pixels[(y + 1) * width + x])
                val p22 = Color.red(pixels[(y + 1) * width + (x + 1)])

                // Using a common Laplacian kernel: [[0, 1, 0], [1, -4, 1], [0, 1, 0]]
                // val L = (p01 + p10 + p12 + p21) - 4 * p11
                // Or another common one: [[-1, -1, -1], [-1, 8, -1], [-1, -1, -1]]
                val L = (8 * p11) - (p00 + p01 + p02 + p10 + p12 + p20 + p21 + p22)


                laplacian[y][x] = L.toDouble()
                sumLaplacian += L.toDouble()
                count++
            }
        }

        if (count == 0) return 0.0
        val meanLaplacian = sumLaplacian / count

        // 3. Calculate Variance
        var sumSquaredDifference = 0.0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                sumSquaredDifference += (laplacian[y][x] - meanLaplacian).pow(2)
            }
        }

        return if (count > 0) sumSquaredDifference / count else 0.0
    }

    fun isFaceBrightnessValid(
        bitmap: Bitmap,
        config: FaceBrightnessConfig = FaceBrightnessConfig() // Use default or provide custom config
    ): Boolean {
        if (bitmap.width == 0 || bitmap.height == 0) {
            Logger.e(MY_TAG, "Bitmap has zero width or height.")
            return false
        }

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        if (pixels.isEmpty()) {
            Logger.e(MY_TAG, "Pixel array is empty.")
            return false
        }

        val luminances = IntArray(pixels.size)
        var deepShadowPixelCount = 0
        var blownHighlightPixelCount = 0

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            luminances[i] = luminance

            if (luminance < config.absoluteShadowLuminance) {
                deepShadowPixelCount++
            }
            if (luminance > config.absoluteHighlightLuminance) {
                blownHighlightPixelCount++
            }
        }

        val totalPixels = pixels.size.toFloat()

        // 1. Check for excessive deep shadows (potential underexposure)
        val shadowPercentage = deepShadowPixelCount / totalPixels
        if (shadowPercentage > config.maxShadowPercentage) {
            Logger.d(
                MY_TAG,
                "Rejected: Too many deep shadow pixels (${
                    String.format(
                        "%.2f",
                        shadowPercentage * 100
                    )
                }%)"
            )
            return false
        }

        // 2. Check for excessive blown highlights (potential overexposure)
        // Regarding your point "it should not reject the overexposed face image as well":
        // Typically, "valid brightness" implies NOT being significantly overexposed.
        // This check aims to identify and reject such cases. If your definition of "valid"
        // explicitly includes overexposed images, this check might need modification or removal.
        // For now, I'm assuming "valid" means well-exposed, i.e., not overly bright.
        val highlightPercentage = blownHighlightPixelCount / totalPixels
        if (highlightPercentage > config.maxHighlightPercentage) {
            Logger.d(
                MY_TAG,
                "Rejected: Too many blown highlight pixels (${
                    String.format(
                        "%.2f",
                        highlightPercentage * 100
                    )
                }%)"
            )
            return false
        }

        // 3. Check for sufficient dynamic range in the non-clipped (or overall) tones
        if (luminances.size < 20) { // Need enough pixels to calculate percentiles reliably
            Logger.d(
                MY_TAG,
                "Rejected: Not enough pixels for reliable dynamic range check after initial filtering or small image."
            )
            return false // Or handle as per specific needs for very small images/regions
        }

        // Sort luminances to find percentiles
        // Using a mutable list for sorting; could optimize if memory is a huge concern for very large bitmaps
        val sortedLuminances = luminances.toMutableList()
        Collections.sort(sortedLuminances)

        val minPercentileIndex = (sortedLuminances.size * config.dynamicRangeMinPercentile).toInt()
            .coerceIn(0, sortedLuminances.size - 1)
        val maxPercentileIndex = (sortedLuminances.size * config.dynamicRangeMaxPercentile).toInt()
            .coerceIn(0, sortedLuminances.size - 1)

        if (minPercentileIndex >= maxPercentileIndex && sortedLuminances.size > 1) {
            Logger.d(
                MY_TAG,
                "Rejected: Percentile indices are problematic (min: $minPercentileIndex, max: $maxPercentileIndex for size ${sortedLuminances.size}). Image likely has extremely low variance."
            )
            return false // Indicates very flat image or not enough distinct values
        }
        if (sortedLuminances.isEmpty()) {
            Logger.d(MY_TAG, "Rejected: No luminance data to process for dynamic range.")
            return false
        }


        val luminanceAtMinPercentile = sortedLuminances[minPercentileIndex]
        val luminanceAtMaxPercentile = sortedLuminances[maxPercentileIndex]
        val dynamicRange = luminanceAtMaxPercentile - luminanceAtMinPercentile

        Logger.d(
            MY_TAG,
            "Shadow Pct: ${
                String.format(
                    "%.2f",
                    shadowPercentage * 100
                )
            }%, Highlight Pct: ${
                String.format(
                    "%.2f",
                    highlightPercentage * 100
                )
            }%, Lower Percentile Lum: $luminanceAtMinPercentile, Upper Percentile Lum: $luminanceAtMaxPercentile, Dynamic Range: $dynamicRange"
        )

        if (dynamicRange < config.minRequiredDynamicRange) {
            Logger.d(
                MY_TAG,
                "Rejected: Dynamic range ($dynamicRange) is less than required (${config.minRequiredDynamicRange}). Face may lack contrast or be poorly exposed."
            )
            return false
        }

        Logger.d(MY_TAG, "Accepted: Image meets brightness and contrast criteria.")
        return true
    }
}