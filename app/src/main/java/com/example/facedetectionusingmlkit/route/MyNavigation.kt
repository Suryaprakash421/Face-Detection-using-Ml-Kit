package com.example.facedetectionusingmlkit.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.ui.screens.setting.Settings
import com.example.facedetectionusingmlkit.ui.screens.ai.AiScreen
import com.example.facedetectionusingmlkit.ui.screens.home.HomeScreen

@Composable
fun MyNavigation(navController: NavHostController, prefManager: PrefManager) {
    NavHost(navController = navController, startDestination = Home.route) {
        composable(Home.route) {
            HomeScreen()
        }
        composable(Ai.route) {
            AiScreen()
        }
        composable(Settings.route) {
            Settings(prefManager)
        }
    }
}