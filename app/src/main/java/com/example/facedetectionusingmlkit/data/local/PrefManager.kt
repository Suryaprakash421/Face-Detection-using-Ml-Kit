package com.example.facedetectionusingmlkit.data.local

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

class PrefManager(
    private val sp: SharedPreferences
) {

    companion object {
        private const val PROCESSED_TIMES = "processed_times"
        private const val MEM_USAGE = "memory_usage"
        private const val AI_ENABLED = "ai_enabled"
        private const val MAX_THRESHOLD = "maximum_threshold"
        private const val MIN_THRESHOLD = "minimum_threshold"
        private const val MIN_FACE_SIZE = "minimum_face_size"
        private const val FACE_DETECTION_MODE = "face_detection_mode"
        private const val USE_HEIC_DECODER = "use_heic_decoder"
        private const val FACE_PADDING = "face_padding"
        private const val HEIC_IMAGE_PROCESS_TIME = "heic_image_time"
        private const val NORMAL_IMAGE_PROCESS_TIME = "normal_image_time"
        private const val ML_KIT_PROCESS_TIME = "ml_kit_process_time"
        private const val IMAGE_WIDTH = "image_width"
        private const val IMAGE_HEIGHT = "image_height"
    }

    /**
     * Adds a new time to the list and stores it.
     */
    fun addProcessedTime(time: Long) {
        val timesList = getProcessedTimes().toMutableList()
        timesList.add(time)
        sp.edit() { putString(PROCESSED_TIMES, timesList.joinToString(",")) }
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
        sp.edit() { putString(PROCESSED_TIMES, null) }
        sp.edit() { putString(NORMAL_IMAGE_PROCESS_TIME, null) }
        sp.edit() { putString(HEIC_IMAGE_PROCESS_TIME, null) }
    }

    /**
     * Adds a memory usage.
     */
    fun addMemoryUsage(mem: Double) {
        val memList = gddMemoryUsage().toMutableList()
        memList.add(mem)
        sp.edit() { putString(MEM_USAGE, memList.joinToString(",")) }
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
     * Retrieves the Minimum mem used.
     */
    fun getMinMemUsed(): Double {
        return gddMemoryUsage().minOrNull() ?: 0.0
    }

    /**
     * Retrieves the Average mem used.
     */
    fun getAvgMemUsed(): Double {
        return gddMemoryUsage().average() ?: 0.0
    }

    /**
     * Retrieves the Maximum mem used.
     */
    fun resetMemUsage() {
        sp.edit() { putString(MEM_USAGE, null) }
    }

    /**
     * Get and set face detection enabled
     * */
    fun setAiEnabledState(isEnabled: Boolean) {
        sp.edit() { putBoolean(AI_ENABLED, isEnabled) }
    }

    fun getAiEnabledState(): Boolean = sp.getBoolean(AI_ENABLED, false)

    /**
     * Set max threshold
     * */
    fun setMaxThreshold(threshold: Float) {
        sp.edit() { putFloat(MAX_THRESHOLD, threshold) }
    }

    fun getMaxThreshold(): Float = sp.getFloat(MAX_THRESHOLD, 0.8f)

    /**
     * Set min threshold
     * */
    fun setMinThreshold(threshold: Float) {
        sp.edit() { putFloat(MIN_THRESHOLD, threshold) }
    }

    fun getMinThreshold(): Float = sp.getFloat(MIN_THRESHOLD, 0.6f)

    /**
     * Set min face size to detect
     * */
    fun setMinFaceSize(minSize: Float) {
        sp.edit() { putFloat(MIN_FACE_SIZE, minSize) }
    }

    fun getMinFaceSize(): Float = sp.getFloat(MIN_FACE_SIZE, 0.5f)

    /**
     * Set min face size to detect
     * */
    fun setIsFaceDetectionModeAccurate(isFast: Boolean) {
        sp.edit() { putBoolean(FACE_DETECTION_MODE, isFast) }
    }

    fun isFaceDetectionModeAccurate(): Boolean = sp.getBoolean(FACE_DETECTION_MODE, false)

    /**
     * Set min face size to detect
     * */
    fun setIsHeicDecoder(isTrue: Boolean) {
        sp.edit() { putBoolean(USE_HEIC_DECODER, isTrue) }
    }

    fun isHeicDecoder(): Boolean = sp.getBoolean(USE_HEIC_DECODER, true)

    /**
     * Set face padding
     * */
    fun setFacePadding(minSize: Float) {
        sp.edit() { putFloat(FACE_PADDING, minSize) }
    }

    fun getFacePadding(): Float = sp.getFloat(FACE_PADDING, 0.07f)

    /**
     * Update the time taken for HEIC and Other images to create the bitmap
     */
    fun addSingleImageProcessTime(time: Long, mimeType: String?) {
        if (mimeType?.contains("heic") == true) {
            val timesList = getHeicProcessedTime().toMutableList()
            timesList.add(time)
            sp.edit() { putString(HEIC_IMAGE_PROCESS_TIME, timesList.joinToString(",")) }
        } else {
            val timesList = getNormalImageProcessedTime().toMutableList()
            timesList.add(time)
            sp.edit() { putString(NORMAL_IMAGE_PROCESS_TIME, timesList.joinToString(",")) }
        }
    }

    /**
     * Retrieves the list of stored HEIC images process time.
     */
    fun getHeicProcessedTime(): List<Long> {
        val storedString = sp.getString(HEIC_IMAGE_PROCESS_TIME, null) ?: return emptyList()
        return storedString.split(",").mapNotNull { it.toLongOrNull() }
    }

    /**
     * Retrieves the list of stored Normal images process times.
     */
    fun getNormalImageProcessedTime(): List<Long> {
        val storedString = sp.getString(NORMAL_IMAGE_PROCESS_TIME, null) ?: return emptyList()
        return storedString.split(",").mapNotNull { it.toLongOrNull() }
    }

    /**
     * Retrieves the average processed time for Normal images.
     */
    fun getAverageNormalImageProcessedTime(): Long {
        val timesList = getNormalImageProcessedTime()
        Log.i("timesList", "timesList: $timesList")
        return if (timesList.isNotEmpty()) timesList.average().toLong() else 0L
    }

    /**
     * Retrieves the average processed time for HEIC images.
     */
    fun getAverageHeicImageProcessedTime(): Long {
        val timesList = getHeicProcessedTime()
        Log.i("timesList", "timesList: $timesList")
        return if (timesList.isNotEmpty()) timesList.average().toLong() else 0L
    }

    /**
     * Add ml kit process time.
     */
    fun addMlProcessTime(time: Long) {
        val timesList = getMlProcessTime().toMutableList()
        timesList.add(time)
        sp.edit() { putString(ML_KIT_PROCESS_TIME, timesList.joinToString(",")) }
    }

    /**
     * Retrieves the list of ml process times.
     */
    private fun getMlProcessTime(): List<Long> {
        val storedString = sp.getString(ML_KIT_PROCESS_TIME, null) ?: return emptyList()
        return storedString.split(",").mapNotNull { it.toLongOrNull() }
    }

    /**
     * Retrieves the average ml processed time.
     */
    fun getAverageMlKitProcessedTime(): Long {
        val timesList = getMlProcessTime()
        Log.i("timesList", "timesList: $timesList")
        return if (timesList.isNotEmpty()) timesList.average().toLong() else 0L
    }

    /**
     * Set image width and height
     * */
    fun setWidthAndHeight(size: Int, isHeight: Boolean){
        sp.edit() { putInt(if (isHeight) IMAGE_HEIGHT else IMAGE_WIDTH, size) }
    }

    fun getImageWidth(): Int = sp.getInt(IMAGE_WIDTH, 512)
    fun getImageHeight(): Int = sp.getInt(IMAGE_HEIGHT, 512)
}