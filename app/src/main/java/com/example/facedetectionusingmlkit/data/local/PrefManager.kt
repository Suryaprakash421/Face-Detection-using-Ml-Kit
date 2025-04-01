package com.example.facedetectionusingmlkit.data.local

import android.content.SharedPreferences
import android.util.Log

class PrefManager(
    private val sp: SharedPreferences
) {

    companion object {
        private const val PROCESSED_TIMES = "processed_times"
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

}