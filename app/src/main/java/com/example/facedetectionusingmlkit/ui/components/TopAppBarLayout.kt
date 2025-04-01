package com.example.facedetectionusingmlkit.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@Composable
fun TopAppBar(prefManager: PrefManager, myViewModel: MyViewModel = hiltViewModel()) {
    val photoList by myViewModel.galleryImages.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalPhotoCount = photoList.size
    val processedPhotoCount = photoList.filter { it.isProcessed }.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total: $totalPhotoCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Processed: $processedPhotoCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "AvgTime for ${Config.PARALLEL_COUNT} photos: ${formatTime(prefManager.getAverageProcessedTime())}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "TotTime: ${formatTime(prefManager.getProcessedTimes().sum())}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Max memory: ${formatDoubt(prefManager.getMaxMemUsed())} MB",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { handleRestart(myViewModel) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Worker",
                    tint = Color.Black,
                )
            }

            IconButton(onClick = { handleDelete(myViewModel) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear database",
                    tint = Color.Black,
                )
            }
        }
    }
}

private fun formatDoubt(input: Double): String {
    return "%.2f".format(input)
}

private fun formatTime(time: Long): String {
    val seconds = time / 1000.0

    return when {
        time < 1000 -> "${time} ms" // Show milliseconds if less than 1 second
        seconds < 60 -> String.format("%.2f s", seconds) // Show seconds if less than a minute
        else -> {
            val minutes = (seconds / 60).toInt()
            val remainingSeconds = seconds % 60
            String.format("%02d:%04.1f m", minutes, remainingSeconds) // Show mm:ss.s
        }
    }
}


private fun handleDelete(myViewModel: MyViewModel) {
    Log.i("ButtonClick", "Delete clicked")
    myViewModel.resetGalleryTable()
}

private fun handleRestart(myViewModel: MyViewModel) {
    myViewModel.startFaceDetectionWorker()
}