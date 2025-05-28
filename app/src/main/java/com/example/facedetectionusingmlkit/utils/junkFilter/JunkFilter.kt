package com.example.facedetectionusingmlkit.utils.junkFilter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.example.facedetectionusingmlkit.utils.HeicDecoderUtil
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.example.facedetectionusingmlkit.utils.Logger
import java.nio.ByteBuffer
import java.security.MessageDigest


class JunkFilter @Inject constructor(
    @ApplicationContext val context: Context
) {
    companion object {
        private const val MY_TAG = "JunkFilter"
    }

    private val faceDetectionMode = FaceDetectorOptions.PERFORMANCE_MODE_FAST

    private val option = FaceDetectorOptions.Builder()
        .setPerformanceMode(faceDetectionMode)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()

    private val faceDetector by lazy { FaceDetection.getClient(option) }

    val ocrTextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val spamTextKeywords = listOf(
        "good morning",
        "jai shree ram",
        "blessings",
        "offer",
        "please forward",
        "shubh",
        "motivational",
        "sale"
    )

    private val recentHashes = mutableSetOf<String>()

    suspend fun isRelevantFamilyOrFriendPhoto(file: File): Boolean {
        try {
            val fileUri = Uri.fromFile(file)
            val inputImage = InputImage.fromFilePath(context, fileUri)

            Logger.i(MY_TAG, "Start filtering -- photoName: ${file.name} - fileUri: $fileUri")
            Logger.i(MY_TAG, "photoName: ${file.name} - Entered to ML Kit for face check")
            // Step 1: Face detection
            val faces = runMlKit(inputImage)
            val faceCount = faces.size

            Logger.i(MY_TAG, "photoName: ${file.name} - faceCount: $faceCount")

            if (faceCount == 0) return false // no face, likely spam
            if (faceCount > 1) return true   // group/family photo — accept immediately

            Logger.i(MY_TAG, "photoName: ${file.name} - Entered to OCR text check")
            // OCR check
            val ocrText = runOcr(inputImage).lowercase()

            if (ocrText.isNotBlank() && spamTextKeywords.any { ocrText.contains(it) }) {
                Logger.i(MY_TAG, "photoName: ${file.name} - OCR text is not empty")
                return false // detected spam text
            }

            Logger.i(MY_TAG, "photoName: ${file.name} - OCR text is empty and entered to blur check")
            // Step 2: 1 face — apply full filtering
            val bitmap = HeicDecoderUtil.decodeBitmap(
                context = context,
                fileUri,
            ) ?: return false

            val isBlurred = isBlurry(bitmap)
            Logger.i(MY_TAG, "photoName: ${file.name} - isBlurred: $isBlurred")
            // Blur check
            if (isBlurry(bitmap)) return false

            Logger.i(MY_TAG, "photoName: ${file.name} - Entered to duplicate check")
            // Duplicate check
            val hash = hashBitmap(bitmap)
            if (recentHashes.contains(hash)) return false
            recentHashes.add(hash)

            Logger.i(MY_TAG, "photoName: ${file.name} - All conditions are satisfied")
            return true
        } catch (e: Exception) {
            Logger.e(MY_TAG, "photoName: ${file.name} - Exception: ${e.message}")
            return false
        }
    }

    private suspend fun runMlKit(inputImage: InputImage): List<Face> =
        suspendCoroutine { continuation ->
            try {
                faceDetector.process(inputImage)
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

    private suspend fun runOcr(image: InputImage): String =
        suspendCoroutine { continuation ->
            try {
                ocrTextRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val recognizedText = visionText.text
                        continuation.resume(recognizedText)
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    private fun hashBitmap(bitmap: Bitmap): String {
        val buffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(buffer.array()).joinToString("") { "%02x".format(it) }
    }


    fun isBlurry(bitmap: Bitmap, threshold: Double = 100.0): Boolean {

        // Resize for faster processing (optional but recommended)
        val scaledBitmap = bitmap.scale(100, 100, false)

        val laplacianKernel = arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, -4, 1),
            intArrayOf(0, 1, 0)
        )

        var sum = 0.0
        var sumOfSquares = 0.0
        var count = 0

        for (y in 1 until scaledBitmap.height - 1) {
            for (x in 1 until scaledBitmap.width - 1) {
                var laplacian = 0.0

                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = scaledBitmap[x + kx, y + ky]
                        val gray =
                            Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11
                        laplacian += laplacianKernel[ky + 1][kx + 1] * gray
                    }
                }

                sum += laplacian
                sumOfSquares += laplacian * laplacian
                count++
            }
        }

        val mean = sum / count
        val variance = (sumOfSquares / count) - mean.pow(2)
        val stdDeviation = sqrt(variance)

        // Log.d("BlurryCheck", "Laplacian StdDev: $stdDeviation")
        return stdDeviation < threshold
    }

}