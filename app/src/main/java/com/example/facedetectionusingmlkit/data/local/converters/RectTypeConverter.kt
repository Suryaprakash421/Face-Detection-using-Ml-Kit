package com.example.facedetectionusingmlkit.data.local.converters

import android.graphics.Rect
import androidx.room.TypeConverter

class RectTypeConverter {

    @TypeConverter
    fun fromRect(rect: Rect?): String? {
        return rect?.let {
            "${it.left},${it.top},${it.right},${it.bottom}"
        }
    }

    @TypeConverter
    fun toRect(rectString: String?): Rect? {
        return rectString?.let {
            val parts = it.split(",").map { part -> part.toInt() }
            if (parts.size == 4) {
                Rect(parts[0], parts[1], parts[2], parts[3])
            } else {
                null
            }
        }
    }
}