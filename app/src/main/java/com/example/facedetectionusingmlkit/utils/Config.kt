package com.example.facedetectionusingmlkit.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.RandomAccessFile

@SuppressLint("StaticFieldLeak")
object Config {

    lateinit var context: Context
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    const val PARALLEL_COUNT = 3

    //    val PARALLEL_COUNT = Runtime.getRuntime().availableProcessors() - 2
    val BATCH_SIZE = calculateBatchSize()

    const val FACES_FOLDER = "faces"

    private fun calculateBatchSize(): Int {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val ramGB = getTotalRamGB()
        val cpuFreq = getMaxCpuFreqMHz()

        Log.i("SystemConfig", "ram: $ramGB, coreCount: $coreCount, cpuFreq: $cpuFreq")
        // Base batch size
        var batchSize = 50

        // Increase batch size based on performance
        if (coreCount >= 8 && ramGB >= 6 && cpuFreq >= 2500) {
            batchSize = 150  // High-end device
        } else if (coreCount >= 6 && ramGB >= 4 && cpuFreq >= 2000) {
            batchSize = 90   // Mid-range device
        }

        return batchSize
    }

    private fun getTotalRamGB(): Int {
        return try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            (memoryInfo.totalMem / (1024 * 1024 * 1024)).toInt() // Convert to GB
        } catch (e: Exception) {
            4 // Default to 4GB if error occurs
        }
    }

    private fun getMaxCpuFreqMHz(): Int {
        return try {
            val reader =
                RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r")
            val maxFreq = reader.readLine().toInt() / 1000 // Convert to MHz
            reader.close()
            maxFreq
        } catch (e: Exception) {
            1800 // Default to 1.8GHz if error occurs
        }
    }

    const val LATIN = 0
    const val CHINESE = 1
    const val DEVANAGARI = 2
    const val JAPANESE = 3
    const val KOREAN = 4

//    val cartoonKeywords =
//        listOf("cartoon", "animation", "illustration", "art", "toy", "paint", "watercolor paint")
//    val fictionKeyword =
//        listOf("fiction")
//    val imageWithTextKeyword =
//        listOf("poster", "paper", "pattern", "asphalt")
//    val isScreenshotKeyword =
//        listOf("screenshot")
//    val humanKeywords =
//        listOf(
//            "person",
//            "people",
//            "man",
//            "woman",
//            "face",
//            "selfie",
//            "portrait",
//            "child",
//            "dude",
//            "fun"
//        )

    // Keywords that strongly indicate a real human presence
    val humanKeywords = setOf(
        "person", "people", "man", "fun", "woman", "face", "selfie", "portrait", "child",
        "adult", "youth", "boy", "girl", "crowd", "human", "figure", "pedestrian",
        "smile", "hairstyle", "jeans", "interaction" // Often co-occur with humans
    )

    // Keywords indicating the image is likely a cartoon, art, or illustration
    val cartoonArtKeywords = setOf(
        "cartoon", "fiction", "animation", "comics", "anime", "manga", "illustration", "drawing",
        "sketch", "artwork", "painting", "watercolor paint", "oil paint", "graphic design",
        "cgi", "computer graphics", "render", "clip art", "line art", "doodle"
    )

    // Keywords indicating the image is likely text-heavy or a document
    val textHeavyKeywords = setOf(
        "text",
        "font",
        "document",
        "paper",
        "asphalt",
        "pattern",
        "form",
        "letter",
        "handwriting",
        "calligraphy",
        "poster",
        "sign",
        "banner",
        "label",
        "book",
        "magazine",
        "newspaper",
        "brochure",
        "flyer",
        "notepad",
        "memo",
        "typography",
        "caption",
        "headline",
        "menu"
    )

    // Keywords indicating the image is likely a screenshot
    val screenshotKeywords = setOf(
        "screenshot", "screen capture", "web page", "software", "application", "user interface",
        "gui", "window", "desktop", "mobile phone screen", "computer screen", "snapshot"
    )

    // Keywords for toys (handle carefully, as humans might be present with toys)
    val toyKeywords = setOf(
        "toy", "doll", "figurine", "action figure", "stuffed animal", "plush", "plaything", "stuffed toy"
    )

    // Keywords for abstract/other things to potentially filter if no human
    val otherFilterKeywords = setOf(
        "pattern",
        "texture",
        "close-up" // If these are dominant and no human, might not be a face photo
    )

    // Confidence thresholds (adjust based on testing)
    const val HUMAN_CONFIDENCE_THRESHOLD = 0.6f
    const val CARTOON_ART_CONFIDENCE_THRESHOLD = 0.5f
    const val TEXT_HEAVY_CONFIDENCE_THRESHOLD = 0.6f
    const val SCREENSHOT_CONFIDENCE_THRESHOLD = 0.7f
    const val TOY_CONFIDENCE_THRESHOLD = 0.5f
}
