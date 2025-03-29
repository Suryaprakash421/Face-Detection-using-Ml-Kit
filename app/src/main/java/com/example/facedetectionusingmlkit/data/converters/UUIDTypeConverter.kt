package com.example.facedetectionusingmlkit.data.converters

import androidx.room.TypeConverter
import java.util.UUID

class UUIDTypeConverter {

    @TypeConverter
    fun fromUUIDList(uuidList: List<UUID>?): String {
        return uuidList?.joinToString(",") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toUUIDList(uuidString: String?): List<UUID> {
//        Log.i("UUIDTypeConverter", "Raw UUID String: $uuidString") // ✅ Add this

        return uuidString?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { uuid ->
                try {
                    UUID.fromString(uuid.trim())
                } catch (e: IllegalArgumentException) {
//                    Log.e("UUIDTypeConverter", "Invalid UUID detected: $uuid") // ✅ Log invalid UUIDs
                    null
                }
            }
            ?: emptyList()
    }


}