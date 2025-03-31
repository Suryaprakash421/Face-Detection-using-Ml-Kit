package com.example.facedetectionusingmlkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.facedetectionusingmlkit.route.MyNavigation
import com.example.facedetectionusingmlkit.ui.components.BottomBarNavigation
import com.example.facedetectionusingmlkit.ui.components.TopAppBar
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


