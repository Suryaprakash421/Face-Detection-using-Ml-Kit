package com.example.facedetectionusingmlkit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock

object Logger {

    private const val LOG_FILE_PATH = "logs"
    private val context by lazy { Config.context }
    private val logFile by lazy { getLogFileDirectory(context) }
    private val lock = ReentrantLock()
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

        // Write to file in background (IO thread)
        CoroutineScope(Dispatchers.IO).launch {
            writeToFile(logMessage)
        }
    }

    private fun writeToFile(message: String) {
        try {
            lock.lock()
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
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
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, logFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Log File"))
    }

    fun deleteLogFile() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!logFile.exists()) {
                Log.i("Logger", "Log file does not exist, nothing to delete.")
                return@launch
            }

            try {
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
}
