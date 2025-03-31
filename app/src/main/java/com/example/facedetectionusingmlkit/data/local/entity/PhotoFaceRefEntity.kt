package com.example.facedetectionusingmlkit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.UUID

@Entity(
    tableName = "PhotoFaceRef",
    primaryKeys = ["photoId", "faceId"],
    foreignKeys = [
        ForeignKey(
            entity = PhotosEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FacesEntity::class,
            parentColumns = ["id"],
            childColumns = ["faceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoFaceRefEntity(
    val photoId: UUID,
    val faceId: UUID,
    val isShared: Boolean
)

