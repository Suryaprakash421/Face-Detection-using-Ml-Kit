package com.example.facedetectionusingmlkit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock

object Logger {

    const val LOG_FILE_PATH = "logs"
    private val context by lazy { Config.context }
    private val logFile by lazy { getLogFileDirectory(context) }
    private val lock = ReentrantLock() // For thread-safe file writing
    private val logDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.getDefault())

    fun i(tag: String, message: String) = log("I", tag, message, Log::i)
    fun d(tag: String, message: String) = log("D", tag, message, Log::d)
    fun e(tag: String, message: String) = log("E", tag, message, Log::e)
    fun w(tag: String, message: String) = log("W", tag, message, Log::w)

    private fun log(
        level: String,
        tag: String,
        message: String,
        logMethod: (String, String) -> Unit
    ) {
        val timestamp = logDateFormat.format(Date())
        val logMessage = "$timestamp\t $level\t $tag\t $message\n"

        // Log to console
        logMethod(tag, message)

        // Log to file
        writeToFile(logMessage)
    }

    private fun writeToFile(message: String) {
        try {
            lock.lock() // Ensure thread safety
            FileWriter(logFile, true).use { writer ->
                writer.append(message)
            }
        } catch (e: Exception) {
            Log.e("LoggerException", "Error writing to log file: ${e.message}")
        } finally {
            lock.unlock()
        }
    }

    private fun getLogFileDirectory(context: Context): File {
        val directory = File(context.filesDir, LOG_FILE_PATH)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, "log.txt")
    }

    fun shareLogFile(context: Context) {
        val logFile = logFile // Reference to the log file

        if (!logFile.exists()) {
            Log.e("Logger", "Log file does not exist")
            return
        }

        // Step 1: Create a content URI using FileProvider
        val authority =
            "${context.packageName}.fileprovider" // Matches the authority in the manifest
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, logFile)

        // Step 2: Create an intent to share the file
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // MIME type for text files
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read permission
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Step 3: Start the sharing activity
        context.startActivity(Intent.createChooser(shareIntent, "Share Log File"))
    }

    fun deleteLogFile() {
        val logFile = logFile // Reference to the log file

        if (!logFile.exists()) {
            Log.i("Logger", "Log file does not exist, nothing to delete.")
            return
        }

        try {
            // Attempt to delete the file
            val isDeleted = logFile.delete()

            if (isDeleted) {
                Log.i("Logger", "Log file deleted successfully.")
            } else {
                Log.e("Logger", "Failed to delete log file.")
            }
        } catch (e: Exception) {
            Log.e("Logger", "Error while deleting log file: ${e.message}")
        }
    }
}