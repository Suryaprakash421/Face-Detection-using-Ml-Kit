package com.example.facedetectionusingmlkit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

interface Destination {
    val route: String
    val icon: ImageVector
    val title: String
}

object Home : Destination {
    override val route: String
        get() = "Home"
    override val icon: ImageVector
        get() = Icons.Default.Home
    override val title: String
        get() = "Home"
}

object FaceDetection : Destination {
    override val route: String
        get() = "FaceDetection"
    override val icon: ImageVector
        get() = Icons.Default.Face
    override val title: String
        get() = "Ai"
}