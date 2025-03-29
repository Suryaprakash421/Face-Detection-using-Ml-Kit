package com.example.facedetectionusingmlkit.data.entity

import android.graphics.Rect
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.facedetectionusingmlkit.data.converters.FloatArrayTypeConverter
import com.example.facedetectionusingmlkit.data.converters.RectTypeConverter
import com.example.facedetectionusingmlkit.data.converters.SimilarFaceTypeConverter
import com.example.facedetectionusingmlkit.data.converters.UUIDTypeConverter
import com.example.facedetectionusingmlkit.data.converters.UriTypeConverter
import java.util.UUID

@Entity(tableName = "faces")
@TypeConverters(
    FloatArrayTypeConverter::class,
    UriTypeConverter::class,
    UUIDTypeConverter::class,
    RectTypeConverter::class,
    SimilarFaceTypeConverter::class
)
data class FacesEntity(
    @PrimaryKey val id: UUID,
    val refFaceId: UUID?,
    val fileName: String,
    val filePath: String?,
    val identifier: Uri?,
    val contactNumber: String?,
    val faceName: String?,
    val optionalName: String?,
    val itsMe: Boolean,
    val similarFaceList: List<SimilarFaceWithSimilarity>,
    val embedding: FloatArray?,
    val bounding: Rect?,
    val isRemoved: Boolean
)

data class SimilarFaceWithSimilarity(
    val faceId: UUID,
    val similarity: Float
)
