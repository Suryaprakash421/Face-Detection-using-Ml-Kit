package com.example.facedetectionusingmlkit.screens

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.facedetectionusingmlkit.GridPhotoView
import com.example.facedetectionusingmlkit.MyViewModel
import com.example.facedetectionusingmlkit.utils.getGalleryPhotos

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    CheckPermission()
    GridPhotoView()
}

@Composable
fun CheckPermission(modifier: Modifier = Modifier, myViewModel: MyViewModel = hiltViewModel()) {
    val context = LocalContext.current
    RequestMediaPermissions() { isGranted ->
        Log.d("isGranted", "isGranted: $isGranted")
        if (isGranted) {
            val galleryPhotos = getGalleryPhotos(context)
            Log.d("isGranted", "galleryPhotos: ${galleryPhotos.size}")
            myViewModel.insertGalleryImages(galleryPhotos)
        }

    }
}

@Composable
fun RequestMediaPermissions(isGranted: (isGranted: Boolean) -> Unit) {
    val context = LocalContext.current
    var isPermissionGranted by remember {
        mutableStateOf(false)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            isPermissionGranted = true
            isGranted.invoke(true)
            Toast.makeText(context, "Permissions Granted!", Toast.LENGTH_SHORT).show()
        } else {
            isPermissionGranted = false
            isGranted.invoke(false)
            Toast.makeText(context, "Permissions Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val allPermissionsGranted = requiredPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!allPermissionsGranted) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            isPermissionGranted = true
            isGranted.invoke(true)
        }
    }

    Log.i("isPermissionGranted", "isPermissionGranted: $isPermissionGranted")
    if (!isPermissionGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    permissionLauncher.launch(requiredPermissions)
                }
            ) {
                Text("Request Media Permissions")
            }
        }
    }
}
