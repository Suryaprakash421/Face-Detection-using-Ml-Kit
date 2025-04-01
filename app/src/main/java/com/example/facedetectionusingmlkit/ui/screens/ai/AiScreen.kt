package com.example.facedetectionusingmlkit.ui.screens.ai

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.facedetectionusingmlkit.data.local.entity.PhotosEntity
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun AiScreen(myViewModel: MyViewModel = hiltViewModel()) {
    val faceAndPhotoList by myViewModel.faceAndPhotoList.collectAsStateWithLifecycle(initialValue = emptyList())
    Log.i("faceAndPhotoList", "faceAndPhotoList: ${faceAndPhotoList.size}")

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(faceAndPhotoList) { faceAndPhoto ->
            faceAndPhoto.faceData.filePath?.let {
                Column {
                    FaceView(it)
                    Spacer(modifier = Modifier.height(10.dp))
                    PhotoView(faceAndPhoto.photoList)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FaceView(filePath: String) {
    AsyncImage(
        model = filePath, contentDescription = null, modifier = Modifier
            .size(44.dp)
            .clip(
                RoundedCornerShape(50)
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun PhotoView(photoList: List<PhotosEntity>) {
    LazyRow {
        items(photoList) { photo ->
            SubcomposeAsyncImage(model = photo.identifier,
                contentDescription = null,
                modifier = Modifier
                    .size(144.dp)
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
            Spacer(modifier = Modifier.width(10.dp))
        }
    }

}