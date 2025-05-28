package com.example.facedetectionusingmlkit.ui.screens.whatsApp

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facedetectionusingmlkit.ui.components.GridPhotoView
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun WhatsAppGallery(modifier: Modifier = Modifier, myViewModel: MyViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        myViewModel.getWhatsAppPhotos()
    }
    val whatsAppImages by myViewModel.whatsAppImages.collectAsState(initial = emptyList())
    Log.i("whatsAppImages", "whatsAppImages: ${whatsAppImages.size} - $whatsAppImages")
    GridPhotoView(galleryImageList = whatsAppImages)
}