package com.example.facedetectionusingmlkit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.facedetectionusingmlkit.data.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.repositories.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    application: Application,
    private val myRepository: MyRepository
) : AndroidViewModel(application) {

    /**
     * Insert gallery images
     * */
    fun insertGalleryImages(galleryPhotoEntity: List<GalleryPhotoEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            myRepository.insertGalleryImages(galleryPhotoEntity)
        }
    }
}