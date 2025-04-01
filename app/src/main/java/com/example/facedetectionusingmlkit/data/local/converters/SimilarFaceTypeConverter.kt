package com.example.facedetectionusingmlkit.data.local.converters

import androidx.room.TypeConverter
import com.example.facedetectionusingmlkit.data.local.entity.SimilarFaceWithSimilarity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class SimilarFaceTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromSimilarFaceWithSimilarity(value: SimilarFaceWithSimilarity?): String {
        return gson.toJson(value ?: SimilarFaceWithSimilarity(UUID.randomUUID(), 0f))
    }

    @TypeConverter
    fun toSimilarFaceWithSimilarity(value: String?): SimilarFaceWithSimilarity {
        return gson.fromJson(value, object : TypeToken<SimilarFaceWithSimilarity>() {}.type)
            ?: SimilarFaceWithSimilarity(UUID.randomUUID(), 0f)
    }

    @TypeConverter
    fun fromSimilarFaceList(value: List<SimilarFaceWithSimilarity>?): String {
        return gson.toJson(value ?: emptyList<SimilarFaceWithSimilarity>())
    }

    @TypeConverter
    fun toSimilarFaceList(value: String?): List<SimilarFaceWithSimilarity> {
        return gson.fromJson(value, object : TypeToken<List<SimilarFaceWithSimilarity>>() {}.type)
            ?: emptyList()
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String {
        return uuid?.toString() ?: ""
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID {
        return if (uuid.isNullOrBlank()) UUID.randomUUID() else UUID.fromString(uuid)
    }

}