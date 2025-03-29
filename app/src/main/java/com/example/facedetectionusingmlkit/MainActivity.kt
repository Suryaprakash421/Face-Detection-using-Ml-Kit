package com.example.facedetectionusingmlkit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.facedetectionusingmlkit.ui.theme.FaceDetectionUsingMlKitTheme
import com.example.facedetectionusingmlkit.utils.getGalleryPhotos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceDetectionUsingMlKitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CheckPermission()
                    }
                }
            }
        }
    }
}

@Composable
fun CheckPermission(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    RequestMediaPermissions() { isGranted ->
        Log.d("isGranted", "isGranted: $isGranted")
        if (isGranted) {
            val galleryPhotos = getGalleryPhotos(context)
            Log.d("isGranted", "galleryPhotos: ${galleryPhotos.size}")
        }

    }
}
