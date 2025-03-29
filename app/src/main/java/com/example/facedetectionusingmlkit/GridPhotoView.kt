package com.example.facedetectionusingmlkit

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.facedetectionusingmlkit.data.entity.GalleryPhotoEntity
import com.example.facedetectionusingmlkit.utils.getGalleryPhotos

@Composable
fun GridPhotoView(modifier: Modifier = Modifier, myViewModel: MyViewModel = hiltViewModel()) {

    val galleryImageList by myViewModel.galleryImages.collectAsState(initial = emptyList())

    Log.i("galleryImageList", "galleryImageList: $galleryImageList")

    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            modifier = Modifier.padding(0.dp)
        ) {
            itemsIndexed(galleryImageList, key = { _, image -> image.id }) { _, image ->
                PhotoView(image)
            }
        }
    }
}

@Composable
fun PhotoView(galleryPhotoEntity: GalleryPhotoEntity, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(galleryPhotoEntity.fileUri)
            .size(156)
            .build()
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = "Image",
            modifier = modifier
                .fillMaxSize()
                .padding(1.dp)
                .aspectRatio(1f),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        )
    }
}
