package com.example.facedetectionusingmlkit.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import com.example.facedetectionusingmlkit.data.local.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotoFaceRefEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotosEntity
import com.example.facedetectionusingmlkit.data.local.entity.SimilarFaceWithSimilarity
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.utils.Config
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID
import javax.inject.Inject
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

class FaceRecognition @Inject constructor(
    @ApplicationContext private val context: Context,
    private val myRepository: MyRepository
) {
    companion object {
        private const val MODEL_NAME = "facenet_512_int_quantized.tflite"
        private const val MY_TAG = "FaceRecognition"

        const val MAX_THRESHOLD = 0.8
        const val MIN_THRESHOLD = 0.6

        const val MAX_PONT_FOR_DISTANCE = 1.2
        const val MIN_PONT_FOR_DISTANCE = 1.0
    }

    init {
        observeFacesEntity()
    }

    private val options = Interpreter.Options().apply {
        addDelegate(NnApiDelegate())
        numThreads = 1
//        useXNNPACK = true
    }

    fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_NAME)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    private fun getInterpreter(): Interpreter {
        return Interpreter(
            loadModelFile(),
            options
        )
    }

    /**
     * Observe live changes in faces Entity
     * */
    private var facesList: List<FacesEntity> = emptyList()
    private fun observeFacesEntity() {
        CoroutineScope(Dispatchers.IO).launch {
            myRepository.faceListDetails.collectLatest { faces ->
                facesList = faces
            }
        }
    }

    suspend fun processDetectedFace(
        faces: List<Face>,
        originalBitmap: Bitmap,
        photo: GalleryPhotoEntity
    ) = coroutineScope {
        try {
            measureTimeMillis {
                val photoDetail = PhotosEntity(
                    id = UUID.randomUUID(),
                    photoName = photo.photoName,
                    filePath = photo.filePath,
                    capturedDate = photo.capturedDate,
                    identifier = photo.fileUri
                )
                val differedPhotoDetail = async { insertPhotoDetails(photoDetail) }
                differedPhotoDetail.await()
                faces.map { face ->
                    async {
                        val faceBitmap: Bitmap
                        measureTimeMillis {
                            faceBitmap = cropFaceWithPadding(
                                originalBitmap,
                                face,
                                (face.boundingBox.width() * 0.07).toInt()
                            )
                        }.also {
                            Log.i(MY_TAG, "Takes $it ms for crop the face")
                        }
                        val alignedBitmap = if (face.headEulerAngleZ != 0f) {
                            rotateBitmap(faceBitmap, face.headEulerAngleZ)
                        } else {
                            faceBitmap
                        }

                        measureTimeMillis {
                            val embedding =
                                generateEmbedding(alignedBitmap)
                                    ?: return@async
                            compareFaces(photoDetail, embedding, faceBitmap)
                        }.also {
                            Log.d(MY_TAG, "Takes $it ms for generate one embedding")
                        }
                        faceBitmap.recycle()
                        alignedBitmap.recycle()
                    }
                }.awaitAll()
            }.also {
                Log.i(MY_TAG, "Created embedding for ${faces.size} faces takes $it ms")
            }
        } catch (e: Exception) {
            Log.e(MY_TAG, "Exception: ${e.message}")
        }
    }

    fun normalizeBitmap(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val normalizedPixels = FloatArray(width * height * 3)
        var index = 0
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            normalizedPixels[index++] = r
            normalizedPixels[index++] = g
            normalizedPixels[index++] = b
        }
        return normalizedPixels
    }


    private suspend fun insertPhotoDetails(photoDetail: PhotosEntity) {
        myRepository.insertPhoto(photoDetail)
    }

    private suspend fun compareFaces(
        photoDetail: PhotosEntity,
        currentEmbedding: FloatArray,
        faceBitmap: Bitmap
    ) {
        val filteredFaces = getSimilarEmbeddings(currentEmbedding)
        Log.i(MY_TAG, "filteredFaces size: ${filteredFaces.size}")

        if (filteredFaces.isEmpty()) {
            handleNewFace(
                currentEmbedding,
                faceBitmap,
                photoDetail
            )
            return
        }
        val (sameFaceList, similarFaceList) = filteredFaces.partition { isSameFace(it.similarity) }
        Log.i(
            MY_TAG,
            "sameFaceList size: ${sameFaceList.size}, similarFaceList: ${similarFaceList.size}"
        )

        sameFaceList.minByOrNull { it.similarity }?.let { sameFaceData ->
//        sameFaceList.maxByOrNull { it.similarity }?.let { sameFaceData ->
            handleSameFace(sameFaceData, photoDetail)
        } ?: handleNewFace(
            currentEmbedding,
            faceBitmap,
            photoDetail,
            similarFaceList
        )
    }

    private fun isSameFace(similarityValue: Float): Boolean =
        similarityValue >= MAX_THRESHOLD

    private fun getSimilarEmbeddings(currentEmbedding: FloatArray): List<SimilarFaceWithSimilarity> {
        val result = mutableListOf<SimilarFaceWithSimilarity>()
        facesList.mapNotNull { face ->
            face.embedding?.let { refEmbedding ->
                val similarity = cousinSimilarity(currentEmbedding, refEmbedding)
                if (similarity >= MIN_THRESHOLD) {
                    result.add(
                        SimilarFaceWithSimilarity(
                            face.id,
                            similarity
                        )
                    )
                }
            }
        }
        return result
    }

    /**
     * Handle same face
     * */
    private suspend fun handleSameFace(
        refFace: SimilarFaceWithSimilarity,
        photoDetail: PhotosEntity,
    ) {
        Log.d(MY_TAG, "Handle Same face")
        insertFaceAndPhotoDetail(
            PhotoFaceRefEntity(
                photoDetail.id,
                refFace.faceId,
                false
            )
        )
    }

    /**
     * Handle new face
     * */
    private suspend fun handleNewFace(
        currentEmbedding: FloatArray,
        faceBitmap: Bitmap,
        photoDetail: PhotosEntity,
        similarFaceList: List<SimilarFaceWithSimilarity> = emptyList()
    ) = coroutineScope {
        Log.d(MY_TAG, "Handle New face")
        val faceFileName = System.nanoTime().toString()
        saveCroppedFaceToExternalStorage(faceBitmap, faceFileName)?.also { file ->
            val faceDetail = FacesEntity(
                id = UUID.randomUUID(),
                refFaceId = null,
                fileName = file.name,
                filePath = file.absolutePath,
                identifier = Uri.fromFile(file),
                contactNumber = null,
                faceName = null,
                optionalName = null,
                itsMe = false,
                similarFaceList = similarFaceList,
                embedding = currentEmbedding,
                bounding = null,
                isRemoved = false
            )
            val differedFaceEntity = async {
                myRepository.insertFace(faceDetail)
            }
            differedFaceEntity.await()
            val differedFaceAndPhotoRefEntity = async {
                insertFaceAndPhotoDetail(
                    PhotoFaceRefEntity(
                        photoDetail.id,
                        faceDetail.id,
                        false
                    )
                )
            }
            differedFaceAndPhotoRefEntity.await()
        }
    }

    /**
     * Insert PhotoFaceRef entity
     * */
    private suspend fun insertFaceAndPhotoDetail(photoFaceRefEntity: PhotoFaceRefEntity) {
        myRepository.insertFaceAndPhotoDetail(photoFaceRefEntity)
    }

    /**
     * Save the face in local storage
     */
    private fun saveCroppedFaceToExternalStorage(bitmap: Bitmap, fileName: String): File? {
        return try {
            val storageDir =
                File("${context.getExternalFilesDir(Config.FACES_FOLDER)?.absolutePath}")
            if (!storageDir.exists()) storageDir.mkdirs()

            val file = File(storageDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG, 100, out
                )
            }
            file
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Compare face using Cousin similarity
     * */
    private fun cousinSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in embedding1.indices) {
            dotProduct += embedding1[i] * embedding2[i]
            norm1 += embedding1[i] * embedding1[i]
            norm2 += embedding2[i] * embedding2[i]
        }

        norm1 = sqrt(norm1)
        norm2 = sqrt(norm2)

        return if (norm1 > 0 && norm2 > 0) {
            dotProduct / (norm1 * norm2)
        } else 0f
    }

    /**
     * Compare with Euclidean distance
     * */
    private fun euclideanDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        var sumSquaredDifferences = 0f
        for (i in embedding1.indices) {
            val diff = embedding1[i] - embedding2[i]
            sumSquaredDifferences += diff * diff
        }
        return sqrt(sumSquaredDifferences)
    }

    private val outputArray =
        Array(1) { FloatArray(512) }

    private fun generateEmbedding(face: Bitmap): FloatArray? {
        try {
            val inputArray = preprocessImage(face)
            inputArray.rewind()

            val interpreter = getInterpreter()
            interpreter.run(inputArray, outputArray)
            interpreter.close()
            // Return the first (and only) array from the 2D array
            return outputArray[0]
        } catch (e: Exception) {
            Log.e("Similar", "Error generating embedding", e)
            return null
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val inputSize = 160
        // Convert the Bitmap to a Mutable ARGB_8888 version
        val safeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val resizedBitmap = Bitmap.createScaledBitmap(safeBitmap, inputSize, inputSize, true)

        val byteBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize)

        // Debug pixel values
        var maxVal = Float.MIN_VALUE
        var minVal = Float.MAX_VALUE

        intValues.forEach { pixelValue ->
            val r = ((pixelValue shr 16) and 0xFF)
            val g = ((pixelValue shr 8) and 0xFF)
            val b = (pixelValue and 0xFF)

            // Normalize to [-1, 1] instead of [0, 1]
            byteBuffer.putFloat((r - 127.5f) / 127.5f)
            byteBuffer.putFloat((g - 127.5f) / 127.5f)
            byteBuffer.putFloat((b - 127.5f) / 127.5f)

            maxVal = maxOf(maxVal, r.toFloat(), g.toFloat(), b.toFloat())
            minVal = minOf(minVal, r.toFloat(), g.toFloat(), b.toFloat())
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    private suspend fun cropFaceWithPadding(
        bitmap: Bitmap,
        face: Face,
        padding: Int
    ): Bitmap {
        val boundingBox = face.boundingBox
        val paddedBox = addPaddingToBoundingBox(
            boundingBox,
            bitmap.width,
            bitmap.height,
            padding
        )
        return Bitmap.createBitmap(
            bitmap,
            paddedBox.left,
            paddedBox.top,
            paddedBox.width(),
            paddedBox.height()
        )
    }

    private fun addPaddingToBoundingBox(
        originalBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        padding: Int
    ): Rect {
        val left = (originalBox.left - padding).coerceAtLeast(0)
        val top = (originalBox.top - padding).coerceAtLeast(0)
        val right = (originalBox.right + padding).coerceAtMost(imageWidth)
        val bottom = (originalBox.bottom + padding).coerceAtMost(imageHeight)
        return Rect(left, top, right, bottom)
    }

    /**
     * Rotate image bitmap
     * */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}