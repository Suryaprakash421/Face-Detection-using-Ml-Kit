package com.example.facedetectionusingmlkit.utils.textRecognition

import android.content.Context
import android.net.Uri
import android.util.Log
import coil.util.CoilUtils.result
import com.example.facedetectionusingmlkit.utils.Logger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognizer(val context: Context) {

    companion object {
        private const val MY_TAG = "TextRecognizer"
    }

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


    fun processImage(uri: Uri, callback: (String?) -> Unit) {
        // val bitmap = HeicDecoderUtil.decodeBitmap(context, uri) ?: return // If you use this, handle the return accordingly
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            Log.e(MY_TAG, "Error creating InputImage from URI: $uri", e)
            callback(null) // Report failure via callback
            return
        }

        Log.i(MY_TAG, "Processing URI: $uri")

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                callback(recognizedText) // Pass the recognized text back via the callback
            }
            .addOnFailureListener { e ->
                Log.e(MY_TAG, "Text recognition failed", e)
                callback(null) // Report failure via callback
            }
    }

    private fun processTextRecognitionResult(result: Text) {
        val resultText = result.text
        Logger.i(MY_TAG, "resultText: $resultText")

        // Optional: Log detailed results
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockFrame = block.boundingBox
            Logger.i(MY_TAG, "Block: $blockText, Bounds: $blockFrame")
            for (line in block.lines) {
                val lineText = line.text
                Logger.i(MY_TAG, "  Line: $lineText")
                for (element in line.elements) {
                    val elementText = element.text
                    Logger.i(MY_TAG, "    Element: $elementText")
                }
            }
        }
    }
}