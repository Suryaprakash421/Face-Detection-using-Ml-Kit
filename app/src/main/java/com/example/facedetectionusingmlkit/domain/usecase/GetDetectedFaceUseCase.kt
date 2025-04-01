package com.example.facedetectionusingmlkit.domain.usecase

import com.example.facedetectionusingmlkit.data.local.entity.FacesEntity
import com.example.facedetectionusingmlkit.data.repositories.MyRepository
import com.example.facedetectionusingmlkit.domain.model.AiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GetDetectedFaceUseCase(
    private val myRepository: MyRepository
) {

    init {
        observeFacesEntity()
    }

    private var _aiScreenData = MutableStateFlow<List<AiModel>>(emptyList())
    val aiScreenData: StateFlow<List<AiModel>> get() = _aiScreenData

    private suspend fun execute(faces: List<FacesEntity>) = coroutineScope {
        val result = faces.mapNotNull { face ->
            val photoList = myRepository.getPhotosByFaceId(face.id)
            if (photoList.isNotEmpty()) {
                AiModel(
                    faceData = face,
                    photoList = photoList
                )
            } else {
                null
            }
        }
        _aiScreenData.value = result
    }


    private fun observeFacesEntity() {
        CoroutineScope(Dispatchers.IO).launch {
            myRepository.faceListDetails.collectLatest { faces ->
                execute(faces)
            }
        }
    }
}