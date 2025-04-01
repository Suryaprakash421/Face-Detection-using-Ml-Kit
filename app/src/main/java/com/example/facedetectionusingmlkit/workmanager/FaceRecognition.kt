package com.example.facedetectionusingmlkit.workmanager

import android.content.Context
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FaceRecognition(
    private val context: Context,
    private val myRepository: MyRepository
) {
    companion object {
        private const val MODEL_NAME = "facenet_512_int_quantized.tflite"
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
}