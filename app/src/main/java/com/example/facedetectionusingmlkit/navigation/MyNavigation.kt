package com.example.facedetectionusingmlkit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.facedetectionusingmlkit.screens.AiScreen
import com.example.facedetectionusingmlkit.screens.HomeScreen

@Composable
fun MyNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Home.route) {
        composable(Home.route){
            HomeScreen()
        }
        composable(FaceDetection.route) {
            AiScreen()
        }
    }
}