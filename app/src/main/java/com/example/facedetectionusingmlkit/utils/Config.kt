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

    val PARALLEL_COUNT = Runtime.getRuntime().availableProcessors() - 2
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
}
