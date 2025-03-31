package com.example.facedetectionusingmlkit.ui.components

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(modifier: Modifier = Modifier, myViewModel: MyViewModel = hiltViewModel()) {
    CenterAlignedTopAppBar(
        title = { Text(text = "Face Detection", style = MaterialTheme.typography.titleLarge) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = { handleRestart(myViewModel) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Worker",
                    tint = Color.White,
                )
            }

            IconButton(onClick = { handleDelete() }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear database",
                    tint = Color.White,
                )
            }
        },
        modifier = modifier
    )
}

private fun handleDelete() {
    Log.i("ButtonClick", "Delete clicked")
}

private fun handleRestart(myViewModel: MyViewModel) {
    myViewModel.startFaceDetectionWorker()
}