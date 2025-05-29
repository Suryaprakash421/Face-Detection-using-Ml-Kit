package com.example.facedetectionusingmlkit.utils.textRecognition

import android.content.Context
import android.net.Uri
import android.util.Log
import coil.util.CoilUtils.result
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.utils.Logger
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TextRecognizer @Inject constructor(
    @ApplicationContext val context: Context, private val prefManager: PrefManager
) {

    companion object {
        private const val MY_TAG = "TextRecognizer"
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

//    val ocrTextRecognizer =
//        TextRecognition.getClient(recognitionOption())

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

        val ocrTextRecognizer =
            TextRecognition.getClient(recognitionOption())

//        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
//        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        ocrTextRecognizer.process(image).addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                for (block in visionText.textBlocks) {
                    Log.d("OCR", "Detected block: ${block.text}")
                }
                callback(recognizedText) // Pass the recognized text back via the callback
            }.addOnFailureListener { e ->
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