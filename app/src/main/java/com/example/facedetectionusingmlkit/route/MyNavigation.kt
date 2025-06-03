package com.example.facedetectionusingmlkit.route

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.facedetectionusingmlkit.data.local.PrefManager
import com.example.facedetectionusingmlkit.ui.screens.setting.Settings
import com.example.facedetectionusingmlkit.ui.screens.ai.AiScreen
import com.example.facedetectionusingmlkit.ui.screens.home.HomeScreen
import com.example.facedetectionusingmlkit.ui.screens.textReconizer.TestTab
import com.example.facedetectionusingmlkit.ui.screens.whatsApp.WhatsAppTabScreen
import com.example.facedetectionusingmlkit.utils.textRecognition.TextRecognizer

@Composable
fun MyNavigation(
    navController: NavHostController,
    prefManager: PrefManager,
    textRecognizer: TextRecognizer
) {
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
        composable(Playground.route) {
            TestTab(prefManager, textRecognizer)
        }
        composable(WhatsApp.route) {
            WhatsAppTabScreen()
        }
    }
}