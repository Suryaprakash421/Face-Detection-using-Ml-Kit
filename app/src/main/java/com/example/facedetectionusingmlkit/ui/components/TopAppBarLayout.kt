package com.example.facedetectionusingmlkit.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.work.WorkInfo
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.route.Ai
import com.example.facedetectionusingmlkit.route.Home
import com.example.facedetectionusingmlkit.route.Settings
import com.example.facedetectionusingmlkit.utils.Config
import com.example.facedetectionusingmlkit.viewmodel.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    navController: NavHostController,
    prefManager: PrefManager,
    myViewModel: MyViewModel = hiltViewModel()
) {
    val workerState by myViewModel.workerState.collectAsStateWithLifecycle(initialValue = null)

    val isWorkerRunning = workerState == WorkInfo.State.RUNNING
    Log.i("isWorkerRunning", "isWorkerRunning: $isWorkerRunning")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val currentScreen = when (currentDestination) {
        Home.route -> Home
        Ai.route -> Ai
        Settings.route -> Settings
        else -> Home
    }

    CenterAlignedTopAppBar(
        title = {
            Text(text = currentScreen.title)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        actions = {
            if (isWorkerRunning) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(26.dp))
            }
            if (currentScreen.route == Settings.route) {
                IconButton(onClick = { handleDelete(myViewModel) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear database",
                    )
                }
            }
        }
    )
}


private fun handleDelete(myViewModel: MyViewModel) {
    Log.i("ButtonClick", "Delete clicked")
    myViewModel.resetGalleryTable()
}