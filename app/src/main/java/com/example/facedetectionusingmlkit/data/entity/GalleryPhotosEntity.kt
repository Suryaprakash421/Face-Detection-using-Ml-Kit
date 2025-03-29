package com.example.facedetectionusingmlkit.data.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gallery_photo")
data class GalleryPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val fileUri: Uri,
    val photoName: String,
    val filePath: String,
    val capturedDate: Long,
    val isProcessed: Boolean = false,
    val noOfFaces: Int = 0
)
