package com.example.facedetectionusingmlkit.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FloatArrayTypeConverter {

    @TypeConverter
    fun fromFloatArray(array: FloatArray?): String? {
        return array?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toFloatArray(json: String?): FloatArray? {
        return json?.let {
            Gson().fromJson(it, object : TypeToken<FloatArray>() {}.type)
        }
    }
}