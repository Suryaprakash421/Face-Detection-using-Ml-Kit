package com.example.facedetectionusingmlkit.data.local

import android.content.SharedPreferences
import android.util.Log
import kotlin.math.max

class PrefManager(
    private val sp: SharedPreferences
) {

    companion object {
        private const val PROCESSED_TIMES = "processed_times"
        private const val MEM_USAGE = "memory_usage"
    }

    /**
     * Adds a new time to the list and stores it.
     */
    fun addProcessedTime(time: Long) {
        val timesList = getProcessedTimes().toMutableList()
        timesList.add(time)
        sp.edit().putString(PROCESSED_TIMES, timesList.joinToString(",")).apply()
    }

    /**
     * Retrieves the list of stored times.
     */
    fun getProcessedTimes(): List<Long> {
        val storedString = sp.getString(PROCESSED_TIMES, null) ?: return emptyList()
        return storedString.split(",").mapNotNull { it.toLongOrNull() }
    }

    /**
     * Retrieves the average processed time.
     */
    fun getAverageProcessedTime(): Long {
        val timesList = getProcessedTimes()
        Log.i("timesList", "timesList: $timesList")
        return if (timesList.isNotEmpty()) timesList.average().toLong() else 0L
    }

    /**
     * Reset processed time
     * */
    fun resetProcessedTime() {
        sp.edit().putString(PROCESSED_TIMES, "0").apply()
    }

    /**
     * Adds a memory usage.
     */
    fun addMemoryUsage(mem: Double) {
        val memList = gddMemoryUsage().toMutableList()
        memList.add(mem)
        sp.edit().putString(MEM_USAGE, memList.joinToString(",")).apply()
    }

    /**
     * Retrieves the list of memory used.
     */
    fun gddMemoryUsage(): List<Double> {
        val storedString = sp.getString(MEM_USAGE, null) ?: return emptyList()
        return storedString.split(",").mapNotNull { it.toDoubleOrNull() }
    }

    /**
     * Retrieves the Maximum mem used.
     */
    fun getMaxMemUsed(): Double {
        return gddMemoryUsage().maxOrNull() ?: 0.0
    }

    /**
     * Retrieves the Maximum mem used.
     */
    fun resetMemUsage() {
        sp.edit().putString(MEM_USAGE, "0.0").apply()
    }

}