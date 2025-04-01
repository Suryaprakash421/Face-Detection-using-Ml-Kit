package com.example.facedetectionusingmlkit.domain.model

import com.example.facedetectionusingmlkit.data.local.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.local.entity.PhotosEntity

data class AiModel(
    val faceData: FacesEntity,
    val photoList: List<PhotosEntity>
)
