package com.example.facedetectionusingmlkit.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel
import com.example.facedetectionusingmlkit.data.local.entity.GalleryPhotoEntity

@Composable
fun GridPhotoView(modifier: Modifier = Modifier, myViewModel: MyViewModel = hiltViewModel()) {
    val galleryImageList by myViewModel.galleryImages.collectAsState(initial = emptyList())

    Log.i("galleryImageList", "galleryImageList: ${galleryImageList.size}")
    val gridState = rememberSaveable(saver = LazyGridState.Saver) {
        LazyGridState()
    }
    Box(modifier = modifier) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(128.dp),
            modifier = Modifier.padding(0.dp)
        ) {
            Log.i("galleryImageList", "Inside Lazy")
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

        Text(
            text = galleryPhotoEntity.noOfFaces.toString(),
            fontSize = 12.sp,
            color = Color.Blue,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(
                    Color.White,
                    RoundedCornerShape(5.dp)
                )
                .padding(5.dp)
                .shadow(5.dp, RectangleShape)
        )
    }
}
