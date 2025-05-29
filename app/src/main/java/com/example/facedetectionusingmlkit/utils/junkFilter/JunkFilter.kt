package com.example.facedetectionusingmlkit.utils.junkFilter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Size
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.utils.HeicDecoderUtil
import com.example.facedetectionusingmlkit.utils.Logger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class JunkFilter @Inject constructor(
    @ApplicationContext val context: Context,
    private val prefManager: PrefManager
) {
    companion object {
        private const val MY_TAG = "JunkFilter"
    }

    private val option = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val faceDetector by lazy { FaceDetection.getClient(option) }

    private fun recognitionOption(): TextRecognizerOptionsInterface {
        return when (prefManager.getTextRecognition()) {
            Config.LATIN -> TextRecognizerOptions.DEFAULT_OPTIONS
            Config.CHINESE -> ChineseTextRecognizerOptions.Builder().build()
            Config.DEVANAGARI -> DevanagariTextRecognizerOptions.Builder().build()
            Config.JAPANESE -> JapaneseTextRecognizerOptions.Builder().build()
            Config.KOREAN -> KoreanTextRecognizerOptions.Builder().build()
            else -> TextRecognizerOptions.DEFAULT_OPTIONS
        }
    }

    val ocrTextRecognizer =
        TextRecognition.getClient(recognitionOption())
//    val ocrTextRecognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

    private val recentHashes = mutableSetOf<String>()

    suspend fun isRelevantFamilyOrFriendPhoto(photo: GalleryPhotoEntity): Boolean {
        var bitmap: Bitmap? = null
        try {
            val fileUri = photo.fileUri
            val inputImage = InputImage.fromFilePath(context, fileUri)

            Logger.i(MY_TAG, "Start filtering -- photoName: ${photo.photoName} - fileUri: $fileUri")
            Logger.i(MY_TAG, "photoName: ${photo.photoName} - Entered to ML Kit for face check")
            // Step 1: Face detection
            val faces = runMlKit(inputImage)
            val faceCount = faces.size

            Logger.i(MY_TAG, "photoName: ${photo.photoName} - faceCount: $faceCount")

            if (faceCount == 0) return false // no face, likely spam
//            if (faceCount > 1) return true   // group/family photo — accept immediately

            Logger.i(MY_TAG, "photoName: ${photo.photoName} - Entered to OCR text check")
            // OCR check
            val ocrText = runOcr(inputImage).lowercase()

            if (ocrText.isNotBlank()) {
                Logger.i(MY_TAG, "photoName: ${photo.photoName} - OCR text is not empty")
                return false // detected spam text
            }

            Logger.i(
                MY_TAG,
                "photoName: ${photo.photoName} - OCR text is empty and entered to blur check"
            )
            // Step 2: 1 face — apply full filtering
            bitmap = HeicDecoderUtil.decodeBitmap(
                context = context,
                fileUri,
                Size(
                    prefManager.getImageWidth(),
                    prefManager.getImageHeight()
                )
            )

            if (bitmap == null) {
                Logger.i(MY_TAG, "photoName: ${photo.photoName} - Bitmap is null")
                return false
            }

            val isBlurred = isBlurry(bitmap)
            Logger.i(MY_TAG, "photoName: ${photo.photoName} - isBlurred: $isBlurred")
            // Blur check
            if (isBlurred) return false

            Logger.i(MY_TAG, "photoName: ${photo.photoName} - Entered to duplicate check")
            // Duplicate check
            val hash = hashBitmap(bitmap)
            if (recentHashes.contains(hash)) return false
            recentHashes.add(hash)

            Logger.i(MY_TAG, "photoName: ${photo.photoName} - All conditions are satisfied")
            return true
        } catch (e: Exception) {
            Logger.e(MY_TAG, "photoName: ${photo.photoName} - Exception: ${e.message}")
            return false
        } finally {
            bitmap?.recycle()
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

    private fun isBlurry(bitmap: Bitmap): Boolean {
        val width = 64
        val height = 64

        val resized = bitmap.scale(width, height, false)
        val gray = createBitmap(width, height)

        val canvas = Canvas(gray)
        val paint = Paint().apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(
                android.graphics.ColorMatrix().apply { setSaturation(0f) }
            )
        }

        canvas.drawBitmap(resized, 0f, 0f, paint)

        val pixels = IntArray(width * height)
        gray.getPixels(pixels, 0, width, 0, 0, width, height)

        val avg = pixels.sumOf { it and 0xff } / pixels.size
        return avg < 40
    }

}
