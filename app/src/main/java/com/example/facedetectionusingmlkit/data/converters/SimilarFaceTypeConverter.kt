package com.example.facedetectionusingmlkit.data.converters

import androidx.room.TypeConverter
import com.example.facedetectionusingmlkit.data.entity.SimilarFaceWithSimilarity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class SimilarFaceTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromSimilarFaceWithSimilarity(value: SimilarFaceWithSimilarity): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSimilarFaceWithSimilarity(value: String): SimilarFaceWithSimilarity {
        return gson.fromJson(value, object : TypeToken<SimilarFaceWithSimilarity>() {}.type)
    }

    // ✅ NEW: Convert List<SimilarFaceWithSimilarity> to String
    @TypeConverter
    fun fromSimilarFaceList(value: List<SimilarFaceWithSimilarity>?): String {
        return gson.toJson(value)
    }

    // ✅ NEW: Convert String back to List<SimilarFaceWithSimilarity>
    @TypeConverter
    fun toSimilarFaceList(value: String): List<SimilarFaceWithSimilarity> {
        return gson.fromJson(value, object : TypeToken<List<SimilarFaceWithSimilarity>>() {}.type)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }
}

