package com.example.facedetectionusingmlkit.data.local.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.facedetectionusingmlkit.data.local.converters.UriTypeConverter
import java.util.UUID

@Entity(tableName = "photos")
@TypeConverters(UriTypeConverter::class)
data class PhotosEntity(
    @PrimaryKey
    val id: UUID,
    val photoName: String,
    val filePath: String,
    val identifier: Uri,
    val capturedDate: Long,
)
