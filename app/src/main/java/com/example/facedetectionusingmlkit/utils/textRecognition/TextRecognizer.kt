package com.example.facedetectionusingmlkit.utils.textRecognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.utils.Config
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
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import com.example.facedetectionusingmlkit.domain.model.OcrResult
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class TextRecognizer @Inject constructor(
    @ApplicationContext val context: Context, private val prefManager: PrefManager
) {

    companion object {
        private const val MY_TAG = "TextRecognizer"
        private const val MODEL_NAME = "object_detection.tflite"

        // Person
        private const val CONFIDENCE_THRESHOLD = 0.1f // Adjust as needed
        private const val IOU_THRESHOLD = 0.5f        // For NMS, adjust as needed
        private const val PERSON_CLASS_ID = 0         // For COCO dataset

        private const val YOLO_INPUT_WIDTH = 320
        private const val YOLO_INPUT_HEIGHT = 320
        private const val NUM_CHANNELS = 3 // RGB
        private const val BYTES_PER_CHANNEL = 4 // Float32
    }

    private val interpreter: Interpreter by lazy {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        Interpreter(loadModelFile(), options)
    }

    private fun loadModelFile(): MappedByteBuffer {
        context.assets.openFd(MODEL_NAME).use { afd ->
            FileInputStream(afd.fileDescriptor).channel.use { channel ->
                return channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    afd.startOffset,
                    afd.declaredLength
                )
            }
        }
    }

    private fun recognitionOption(): TextRecognizerOptionsInterface {
        val selected = prefManager.getTextRecognition()
        Log.i("selected", "selected: $selected")
        return when (prefManager.getTextRecognition()) {
            Config.LATIN -> TextRecognizerOptions.DEFAULT_OPTIONS
            Config.CHINESE -> ChineseTextRecognizerOptions.Builder().build()
            Config.DEVANAGARI -> DevanagariTextRecognizerOptions.Builder().build()
            Config.JAPANESE -> JapaneseTextRecognizerOptions.Builder().build()
            Config.KOREAN -> KoreanTextRecognizerOptions.Builder().build()
            else -> TextRecognizerOptions.DEFAULT_OPTIONS
        }
    }

    private val option = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setLandmarkMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val faceDetector by lazy { FaceDetection.getClient(option) }

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

    val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)



    private suspend fun runImageLabelling(inputImage: InputImage): OcrResult =
        suspendCoroutine { continuation ->
            try {
                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        Logger.i(MY_TAG, "labels: $labels")
                        val labelTexts = labels.map { it.text.lowercase() }
                        Logger.i(MY_TAG, "text: $labelTexts")
                        val hasHuman = labelTexts.any { it in Config.humanKeywords }
                        val hasCartoon = labelTexts.any { it in Config.cartoonKeywords }
                        val isFiction = labelTexts.any { it in Config.fictionKeyword }
                        val hasText = labelTexts.any { it in Config.imageWithTextKeyword }
                        val isScreenshot = labelTexts.any { it in Config.isScreenshotKeyword }

                        Logger.i(MY_TAG, "hasHuman: $hasHuman, hasCartoon: $hasCartoon")

                        val isCartoonImage = isFiction || (!hasHuman && hasCartoon)
                        continuation.resume(
                            OcrResult(
                                labels = labelTexts.toString(),
                                isCartoon = isCartoonImage,
                                hasText = hasText,
                                isScreenshot = isScreenshot
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

    val ocrTextRecognizer =
        TextRecognition.getClient(recognitionOption())

    suspend fun processImage(uri: Uri, callback: (OcrResult?) -> Unit) {
        // val bitmap = HeicDecoderUtil.decodeBitmap(context, uri) ?: return // If you use this, handle the return accordingly
        val inputImage: InputImage
        try {
            inputImage = InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            Log.e(MY_TAG, "Error creating InputImage from URI: $uri", e)
            callback(null) // Report failure via callback
            return
        }

        Log.i(MY_TAG, "Processing URI: $uri")
//        val faces = runMlKit(image)
//        Logger.i(MY_TAG, "faces: ${faces.size}")
//        if (faces.isNotEmpty()) {
//            val bitmap = HeicDecoderUtil.decodeBitmap(
//                context = context,
//                uri,
//                Size(
//                    prefManager.getImageWidth(),
//                    prefManager.getImageHeight()
//                )
//            )
//            Logger.i(MY_TAG, "bitmap: ${bitmap != null}")
//            bitmap?.let {
//                val filtered = filterHumanFacesWithYOLO(it, faces)
//                Logger.i(MY_TAG, "filtered faces: ${filtered.size}")
//            }
//        }

        val result = runImageLabelling(inputImage)
        Log.i(MY_TAG, "isCartoon: $result")


//        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
//        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val ocrText = runOcr(inputImage).lowercase()
        callback.invoke(
            result.copy(ocrText = ocrText)
        )
    }

    private suspend fun runOcr(image: InputImage): String =
        suspendCoroutine { continuation ->
            try {
                val ocrTextRecognizer =
                    TextRecognition.getClient(recognitionOption())
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

    // In your function where you process ML Kit faces
    private suspend fun filterHumanFacesWithYOLO(
        originalBitmap: Bitmap,
        mlKitFaces: List<Face>
    ): List<Face> {

        val confirmedHumanFaces = mutableListOf<Face>()

        try {
            for (mlKitFace in mlKitFaces) {
                val faceCropBitmap =
                    cropBitmapFromRect(originalBitmap, mlKitFace.boundingBox) // Your cropping logic

                Logger.i(MY_TAG, "width: ${faceCropBitmap.width}, height: ${faceCropBitmap.height}")

                if (faceCropBitmap.width <= 1 || faceCropBitmap.height <= 1) continue // Skip tiny/invalid crops

                val yoloInputBuffer = preprocessImageForYOLO(faceCropBitmap)

                Logger.d(MY_TAG, "yoloInputBuffer: $yoloInputBuffer")
                // Prepare output buffer (ensure size matches your model)
                val outputTensor = interpreter.getOutputTensor(0) // Get the actual output tensor
                val outputShape = outputTensor.shape() // e.g., [1, 6300, 85]
                val outputDataType = outputTensor.dataType()

                val numBoxes = outputShape[1]            // This will be 6300
                val numOutputChannels = outputShape[2]   // This will be 85

                val bytesPerOutputChannel =
                    if (outputDataType == org.tensorflow.lite.DataType.UINT8) 1 else 4

// Dynamically calculate buffer size
                val outputBufferSize =
                    outputShape.fold(1) { acc, i -> acc * i } * bytesPerOutputChannel
                val outputBuffer = ByteBuffer.allocateDirect(outputBufferSize)
                outputBuffer.order(ByteOrder.nativeOrder())
                Logger.i(MY_TAG, "outputBuffer: $outputBuffer")

                interpreter.run(yoloInputBuffer, outputBuffer)

                val yoloDetections = postprocessYOLOOutput(
                    outputBuffer,
                    YOLO_INPUT_WIDTH, // The width the crop was resized to for YOLO
                    YOLO_INPUT_HEIGHT, // The height the crop was resized to for YOLO
                    numBoxes,
                    numOutputChannels
                )

                Logger.d(MY_TAG, "yoloDetections: $yoloDetections")

                // Check if any "person" was detected with high confidence within the crop
                val isHumanDetectedByYOLO = yoloDetections.any {
                    it.className == "person" && it.confidence > CONFIDENCE_THRESHOLD // Redundant check if postprocess already filters by class
                }

                Logger.i(MY_TAG, "isHumanDetectedByYOLO: $isHumanDetectedByYOLO")

                if (isHumanDetectedByYOLO) {
                    Log.d(
                        "YOLO_Verification",
                        "Face in ${mlKitFace.boundingBox} confirmed as HUMAN."
                    )
                    confirmedHumanFaces.add(mlKitFace)
                } else {
                    Log.d(
                        "YOLO_Verification",
                        "Face in ${mlKitFace.boundingBox} NOT confirmed as human by YOLO (detections: ${yoloDetections.size})."
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e(MY_TAG, "Exception: ${e.message}")
        }
        return confirmedHumanFaces
    }

    private fun preprocessImageForYOLO(bitmap: Bitmap): ByteBuffer {
        val modelInputWidth = YOLO_INPUT_WIDTH.toFloat()   // 320f
        val modelInputHeight = YOLO_INPUT_HEIGHT.toFloat() // 320f

        // Calculate scaling factor to fit while maintaining aspect ratio
        val scaleFactor = minOf(modelInputWidth / bitmap.width, modelInputHeight / bitmap.height)

        val scaledWidth = (bitmap.width * scaleFactor).toInt()
        val scaledHeight = (bitmap.height * scaleFactor).toInt()

        val matrix = Matrix()
        matrix.postScale(scaleFactor, scaleFactor)
        val scaledBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Create a new 320x320 bitmap and paste the scaled image onto it (letterboxing)
        val letterboxedBitmap = Bitmap.createBitmap(
            modelInputWidth.toInt(),
            modelInputHeight.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(letterboxedBitmap)
        canvas.drawColor(Color.rgb(128, 128, 128)) // Pad with gray (common for YOLO)

        val xOffset = (modelInputWidth - scaledWidth) / 2f
        val yOffset = (modelInputHeight - scaledHeight) / 2f
        canvas.drawBitmap(scaledBitmap, xOffset, yOffset, null)

        // Now, convert letterboxedBitmap to ByteBuffer
        val inputBuffer =
            ByteBuffer.allocateDirect(YOLO_INPUT_WIDTH * YOLO_INPUT_HEIGHT * NUM_CHANNELS * BYTES_PER_CHANNEL)
        inputBuffer.order(ByteOrder.nativeOrder())
        inputBuffer.rewind()

        val intValues = IntArray(YOLO_INPUT_WIDTH * YOLO_INPUT_HEIGHT)
        letterboxedBitmap.getPixels(
            intValues,
            0,
            letterboxedBitmap.width,
            0,
            0,
            letterboxedBitmap.width,
            letterboxedBitmap.height
        )

        for (pixelValue in intValues) {
            inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f) // R
            inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)  // G
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)          // B
        }
        return inputBuffer
    }

    // Function to crop (you might already have a way to get the bitmap for the ML Kit face)
    fun cropBitmapFromRect(originalBitmap: Bitmap, cropRect: Rect): Bitmap {
        // Ensure cropRect is within bitmap bounds
        val validCropRect = Rect(
            maxOf(0, cropRect.left),
            maxOf(0, cropRect.top),
            minOf(originalBitmap.width, cropRect.right),
            minOf(originalBitmap.height, cropRect.bottom)
        )
        if (validCropRect.width() <= 0 || validCropRect.height() <= 0) {
            // Return original or a small placeholder if crop is invalid
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Or handle error
        }
        return Bitmap.createBitmap(
            originalBitmap,
            validCropRect.left,
            validCropRect.top,
            validCropRect.width(),
            validCropRect.height()
        )
    }

    data class DetectionResult(
        val boundingBox: RectF, // Using RectF for float coordinates
        val className: String,
        val confidence: Float
    )

    private fun postprocessYOLOOutput(
        outputBuffer: ByteBuffer,
        imageWidth: Int,
        imageHeight: Int,
        numBoxes: Int,             // ✨ Added parameter
        numOutputChannels: Int     // ✨ Added parameter
    ): List<DetectionResult> {
        outputBuffer.rewind() // Ensure buffer is ready to be read from the beginning

        // val numBoxes = 6300 // Now passed as a parameter
        // val numOutputChannels = 85 // Now passed as a parameter

        val detections = mutableListOf<DetectionResult>()

        try {
            for (i in 0 until numBoxes) { // Use the passed numBoxes
                // The rest of your logic for reading floats (xCenter, yCenter, etc.)
                // should be correct since the output is FLOAT32.
                // Ensure you read (numOutputChannels) floats per box.

                val xCenter = outputBuffer.getFloat() * imageWidth
                val yCenter = outputBuffer.getFloat() * imageHeight
                val width = outputBuffer.getFloat() * imageWidth
                val height = outputBuffer.getFloat() * imageHeight
                val objectConfidence = outputBuffer.getFloat()

                // Number of class scores
                val numClasses = numOutputChannels - 5 // (e.g., 85 - 5 = 80)

                if (objectConfidence < CONFIDENCE_THRESHOLD) {
                    // Skip remaining floats for this detection if object confidence is too low
                    for (j in 0 until numClasses) { // numClasses floats to skip
                        outputBuffer.getFloat()
                    }
                    continue
                }

                val classScores = FloatArray(numClasses)

                for (j in 0 until classScores.size) {
                    classScores[j] = outputBuffer.getFloat()
                }

                // ... (rest of your class score processing and detection adding logic) ...
                var maxClassScore = 0f
                var detectedClassId = -1
                for (j in 0 until classScores.size) {
                    if (classScores[j] > maxClassScore) {
                        maxClassScore = classScores[j]
                        detectedClassId = j
                    }
                }

                val finalConfidence = objectConfidence * maxClassScore

                val personClassScore =
                    if (PERSON_CLASS_ID < classScores.size) classScores[PERSON_CLASS_ID] else -1f
                Logger.d(
                    MY_TAG,
                    "Box $i: ObjConf=${
                        String.format(
                            "%.2f",
                            objectConfidence
                        )
                    }, PersonScore=${
                        String.format(
                            "%.2f",
                            personClassScore
                        )
                    }, MaxClassScore=${
                        String.format(
                            "%.2f",
                            maxClassScore
                        )
                    }, DetectedClassID=$detectedClassId"
                )

                if (detectedClassId == PERSON_CLASS_ID && finalConfidence > CONFIDENCE_THRESHOLD) {
                    val xMin = xCenter - width / 2f
                    val yMin = yCenter - height / 2f
                    val xMax = xCenter + width / 2f
                    val yMax = yCenter + height / 2f

                    detections.add(
                        DetectionResult(
                            RectF(xMin, yMin, xMax, yMax),
                            "person",
                            finalConfidence
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e(MY_TAG, "PostProcess -- Exception: ${e.message}")

        }
        return applyNMS(detections)
    }


    // Basic Non-Max Suppression (you might want a more optimized version)
    private fun applyNMS(detections: List<DetectionResult>): List<DetectionResult> {
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val nmsList = mutableListOf<DetectionResult>()

        for (detection in sortedDetections) {
            var keep = true
            for (keptDetection in nmsList) {
                val iou = calculateIOU(detection.boundingBox, keptDetection.boundingBox)
                if (iou > IOU_THRESHOLD) {
                    keep = false
                    break
                }
            }
            if (keep) {
                nmsList.add(detection)
            }
        }
        return nmsList
    }

    private fun calculateIOU(box1: RectF, box2: RectF): Float {
        val xA = maxOf(box1.left, box2.left)
        val yA = maxOf(box1.top, box2.top)
        val xB = minOf(box1.right, box2.right)
        val yB = minOf(box1.bottom, box2.bottom)

        val intersectionArea = maxOf(0f, xB - xA) * maxOf(0f, yB - yA)
        if (intersectionArea == 0f) return 0f

        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)

        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }
}