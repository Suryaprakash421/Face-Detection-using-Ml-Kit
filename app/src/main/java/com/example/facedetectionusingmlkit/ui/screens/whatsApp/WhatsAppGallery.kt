package com.example.facedetectionusingmlkit.ui.screens.whatsApp

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.route.FilteredWhatsAppImages
import com.example.facedetectionusingmlkit.route.WhatsAppImages
import com.example.facedetectionusingmlkit.route.whatsAppTabDestinations
import com.example.facedetectionusingmlkit.ui.components.GridPhotoView
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun WhatsAppTabScreen(
    modifier: Modifier = Modifier,
    myViewModel: MyViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        myViewModel.getWhatsAppPhotos()
    }
    val whatsAppImages by myViewModel.whatsAppImages.collectAsState(initial = emptyList())
    val filteredImages by myViewModel.junkFiltered.collectAsState(initial = emptyList())


    Column(modifier = modifier.fillMaxSize()) {
        // Tab Row
        TabRow(selectedTabIndex = selectedTabIndex) {
            whatsAppTabDestinations.forEachIndexed { index, tab ->
                val count = when (whatsAppTabDestinations[index]) {
                    is WhatsAppImages -> whatsAppImages.size
                    is FilteredWhatsAppImages -> filteredImages.size
                    else -> 0
                }
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text("${tab.title} ($count)") }
                )
            }
        }

        // Content under tabs
        when (whatsAppTabDestinations[selectedTabIndex]) {
            is WhatsAppImages -> WhatsAppGallery(whatsAppImages)
            is FilteredWhatsAppImages -> WhatsAppGallery(filteredImages.toList())
        }
    }
}


@Composable
fun WhatsAppGallery(data: List<GalleryPhotoEntity>, modifier: Modifier = Modifier) {
    Log.i("whatsAppImages", "whatsAppImages: ${data.size} - $data")
    GridPhotoView(galleryImageList = data)
}
