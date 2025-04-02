package com.example.facedetectionusingmlkit.utils

object FormatHelper {

    fun formatTime(time: Long): String {
        val seconds = time / 1000.0

        return when {
            time < 1000 -> "${time} ms" // Show milliseconds if less than 1 second
            seconds < 60 -> String.format("%.2f s", seconds) // Show seconds if less than a minute
            else -> {
                val minutes = (seconds / 60).toInt()
                val remainingSeconds = seconds % 60
                String.format("%02d:%04.1f m", minutes, remainingSeconds) // Show mm:ss.s
            }
        }
    }
}