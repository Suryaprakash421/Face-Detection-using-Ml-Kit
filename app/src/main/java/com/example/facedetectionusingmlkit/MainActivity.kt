package com.example.facedetectionusingmlkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.facedetectionusingmlkit.navigation.FaceDetection
import com.example.facedetectionusingmlkit.navigation.Home
import com.example.facedetectionusingmlkit.navigation.MyNavigation
import com.example.facedetectionusingmlkit.screens.BottomBarNavigation
import com.example.facedetectionusingmlkit.ui.theme.FaceDetectionUsingMlKitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            FaceDetectionUsingMlKitTheme {
                Scaffold(
                    topBar = {
                        TopAppBar()
                    },
                    bottomBar = {
                        BottomAppBar {
                            BottomBarNavigation(navController = navController)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MyNavigation(navController)
                    }
                }
            }
        }
    }
}


